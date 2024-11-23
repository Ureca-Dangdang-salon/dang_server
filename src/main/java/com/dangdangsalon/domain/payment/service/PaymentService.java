package com.dangdangsalon.domain.payment.service;

import com.dangdangsalon.domain.orders.entity.OrderStatus;
import com.dangdangsalon.domain.orders.entity.Orders;
import com.dangdangsalon.domain.orders.repository.OrdersRepository;
import com.dangdangsalon.domain.payment.dto.PaymentCancelRequestDto;
import com.dangdangsalon.domain.payment.dto.PaymentCancelResponseDto;
import com.dangdangsalon.domain.payment.dto.PaymentApproveRequestDto;
import com.dangdangsalon.domain.payment.dto.PaymentApproveResponseDto;
import com.dangdangsalon.domain.payment.entity.Payment;
import com.dangdangsalon.domain.payment.entity.PaymentStatus;
import com.dangdangsalon.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrdersRepository ordersRepository;
    private final PaymentRepository paymentRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final WebClient webClient;
    private static final long IDEMPOTENCY_KEY_TTL = 10 * 60 * 1000L; // 10분 설정

    private static final String IDEMPOTENCY_KEY_PREFIX = "payment:cancel:idempotency:";
    private static final String PAYMENT_IDEMPOTENCY_KEY_PREFIX = "payment:approve:idempotency:";
    private static final String IDEMPOTENCY_KEY_VALUE = "CANCEL_REQUEST_IN_PROGRESS";

    @Value("${toss.api.key}")
    private String tossApiKey;

    @Transactional
    public PaymentApproveResponseDto approvePayment(PaymentApproveRequestDto paymentApproveRequestDto, String idempotencyKey) {
        String redisKey = PAYMENT_IDEMPOTENCY_KEY_PREFIX + idempotencyKey;

        if (saveIdempotencyKey(redisKey)) {
            throw new IllegalStateException("이미 동일한 결제 승인 요청이 처리 중입니다.");
        }

        try {
            validatePaymentAmount(paymentApproveRequestDto.getAmount());
            Orders order = ordersRepository.findByTossOrderId(paymentApproveRequestDto.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("주문 정보를 찾을 수 없습니다: " + paymentApproveRequestDto.getOrderId()));

            if (order.getAmountValue() != paymentApproveRequestDto.getAmount()) {
                throw new IllegalArgumentException("결제 금액이 주문 금액과 일치하지 않습니다.");
            }

            String paymentApprovalUrl = "https://api.tosspayments.com/v1/payments/confirm";
            PaymentApproveResponseDto paymentResponse = sendApprovalRequestToToss(paymentApproveRequestDto, paymentApprovalUrl);

            Payment payment = Payment.builder()
                    .paymentKey(paymentResponse.getPaymentKey())
                    .totalAmount(paymentResponse.getTotalAmount())
                    .paymentStatus(PaymentStatus.ACCEPTED)
                    .requestedAt(paymentResponse.getApprovedAt().toLocalDateTime())
                    .paymentMethod(paymentResponse.getMethod())
                    .orders(order)
                    .build();

            paymentRepository.save(payment);
            order.updateOrderStatus(OrderStatus.ACCEPTED);

            return paymentResponse;
        } finally {
            deleteIdempotencyKey(redisKey);
        }
    }


    private PaymentApproveResponseDto sendApprovalRequestToToss(PaymentApproveRequestDto paymentApproveRequestDto, String url) {
        String authorizationHeader = "Basic " + Base64.getEncoder().encodeToString((tossApiKey + ":").getBytes());

        try {
            return webClient.post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .bodyValue(Map.of(
                            "paymentKey", paymentApproveRequestDto.getPaymentKey(),
                            "orderId", paymentApproveRequestDto.getOrderId(),
                            "amount", paymentApproveRequestDto.getAmount()
                    ))
                    .retrieve()
                    .bodyToMono(PaymentApproveResponseDto.class)
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("Toss 결제 검증 API 호출 실패: {}", ex.getResponseBodyAsString());
            throw new RuntimeException("결제 승인 중 오류가 발생했습니다.");
        }
    }

    @Transactional
    public PaymentCancelResponseDto cancelPayment(PaymentCancelRequestDto paymentCancelRequestDto, String idempotencyKey) {
        String redisKey = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;

        if (saveIdempotencyKey(redisKey)) {
            throw new IllegalStateException("이미 동일한 결제 취소 요청이 처리 중입니다.");
        }

        try {
            Payment payment = paymentRepository.findByPaymentKey(paymentCancelRequestDto.getPaymentKey())
                    .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

            String paymentCancelUrl = "https://api.tosspayments.com/v1/payments/" + paymentCancelRequestDto.getPaymentKey() + "/cancel";
            PaymentCancelResponseDto paymentCancelResponseDto = sendCancelRequestToToss(paymentCancelRequestDto, paymentCancelUrl);

            payment.updatePaymentStatus(PaymentStatus.CANCELED);

            return PaymentCancelResponseDto.builder()
                    .paymentKey(paymentCancelResponseDto.getPaymentKey())
                    .orderId(paymentCancelResponseDto.getOrderId())
                    .status(paymentCancelResponseDto.getStatus())
                    .build();
        } finally {
            deleteIdempotencyKey(redisKey);
        }
    }

    private PaymentCancelResponseDto sendCancelRequestToToss(PaymentCancelRequestDto paymentCancelRequestDto, String url) {
        String authorizationHeader = "Basic " + Base64.getEncoder().encodeToString((tossApiKey + ":").getBytes());

        try {
            return webClient.post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .bodyValue(Map.of(
                            "cancelReason", paymentCancelRequestDto.getCancelReason()
                    ))
                    .retrieve()
                    .bodyToMono(PaymentCancelResponseDto.class)
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("Toss 결제 취소 API 호출 실패: {}", ex.getResponseBodyAsString());
            throw new RuntimeException("결제 취소 중 오류가 발생했습니다.");
        }
    }


    private void validatePaymentAmount(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("결제 금액은 0보다 커야 합니다");
        }
    }

    private boolean saveIdempotencyKey(String key) {
        Boolean isSaved = redisTemplate.opsForValue().setIfAbsent(key, IDEMPOTENCY_KEY_VALUE, Duration.ofMillis(IDEMPOTENCY_KEY_TTL));
        return !Boolean.TRUE.equals(isSaved);
    }

    private void deleteIdempotencyKey(String key) {
        redisTemplate.delete(key);
    }
}
