package com.dangdangsalon.domain.payment.service;

import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.entity.EstimateStatus;
import com.dangdangsalon.domain.estimate.repository.EstimateRepository;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.estimate.request.entity.RequestStatus;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestRepository;
import com.dangdangsalon.domain.orders.entity.OrderStatus;
import com.dangdangsalon.domain.orders.entity.Orders;
import com.dangdangsalon.domain.orders.repository.OrdersRepository;
import com.dangdangsalon.domain.payment.dto.PaymentApproveRequestDto;
import com.dangdangsalon.domain.payment.dto.PaymentApproveResponseDto;
import com.dangdangsalon.domain.payment.dto.PaymentCancelRequestDto;
import com.dangdangsalon.domain.payment.dto.PaymentCancelResponseDto;
import com.dangdangsalon.domain.payment.entity.Payment;
import com.dangdangsalon.domain.payment.entity.PaymentStatus;
import com.dangdangsalon.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrdersRepository ordersRepository;
    private final PaymentRepository paymentRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final WebClient webClient;
    private final PaymentNotificationService paymentNotificationService;
    private final EstimateRequestRepository estimateRequestRepository;
    private final EstimateRepository estimateRepository;

    @Value("${toss.api.key}")
    private String tossApiKey;

    @Value("${toss.api.approve-url}")
    private String tossApproveUrl;

    @Value("${toss.api.cancel-url}")
    private String tossCancelUrl;

    private static final long IDEMPOTENCY_KEY_TTL = 10 * 60 * 1000L; // 10분 설정

    private static final String IDEMPOTENCY_KEY_PREFIX = "payment:cancel:idempotency:";
    private static final String PAYMENT_IDEMPOTENCY_KEY_PREFIX = "payment:approve:idempotency:";

    // 결제 승인(tossAPI)
    @Transactional
    public PaymentApproveResponseDto approvePayment(PaymentApproveRequestDto paymentApproveRequestDto, Long userId) {

        String idempotencyKey = UUID.randomUUID().toString();
        String key = PAYMENT_IDEMPOTENCY_KEY_PREFIX + userId;

        // 멱등키 저장 (중복 요청 방지)
        if (saveIdempotencyKey(key, idempotencyKey)) {
            throw new IllegalStateException("이미 동일한 결제 승인 요청이 처리 중입니다.");
        }

        try {
            // 결제 금액 유효성 검사
            validatePaymentAmount(paymentApproveRequestDto.getAmount());
            Orders order = ordersRepository.findByTossOrderId(paymentApproveRequestDto.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("주문 정보를 찾을 수 없습니다: " + paymentApproveRequestDto.getOrderId()));

            if (order.getAmountValue() != paymentApproveRequestDto.getAmount()) {
                throw new IllegalArgumentException("결제 금액이 주문 금액과 일치하지 않습니다.");
            }

            PaymentApproveResponseDto paymentResponse = sendApprovalRequestToToss(paymentApproveRequestDto, tossApproveUrl, idempotencyKey);

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

            Estimate estimate = estimateRepository.findById(order.getEstimate().getId())
                    .orElseThrow(() -> new IllegalArgumentException("견적서를 찾을 수 없습니다 : " + payment.getOrders().getEstimate().getEstimateRequest().getId()));

            estimate.updateStatus(EstimateStatus.PAID);

            EstimateRequest estimateRequest = estimateRequestRepository.findById(payment.getOrders().getEstimate().getEstimateRequest().getId())
                    .orElseThrow(() -> new IllegalArgumentException("견적 요청을 찾을 수 없습니다 : " + payment.getOrders().getEstimate().getEstimateRequest().getId()));

            estimateRequest.updateRequestStatus(RequestStatus.PAID);

            // 알림
            paymentNotificationService.sendNotificationToUser(order);

            return paymentResponse;
        } catch (Exception e) {
            log.error("결제 승인 처리 중 오류 발생: {}", e.getMessage());
            throw e; // 예외 다시 던지기
        } finally {
            // 멱등키 삭제
            deleteIdempotencyKey(key);
        }
    }

    // 결제 취소(tossAPI)
    @Transactional
    public PaymentCancelResponseDto cancelPayment(PaymentCancelRequestDto paymentCancelRequestDto, Long userId) {

        String idempotencyKey = UUID.randomUUID().toString();
        String key = IDEMPOTENCY_KEY_PREFIX + userId;

        // 멱등키 저장 (중복 요청 방지)
        if (saveIdempotencyKey(key, idempotencyKey)) {
            throw new IllegalStateException("이미 동일한 결제 취소 요청이 처리 중입니다.");
        }

        try {
            Payment payment = paymentRepository.findByPaymentKey(paymentCancelRequestDto.getPaymentKey())
                    .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

            String cancelUrl = tossCancelUrl.replace("{paymentKey}", paymentCancelRequestDto.getPaymentKey());
            PaymentCancelResponseDto paymentCancelResponseDto = sendCancelRequestToToss(paymentCancelRequestDto, cancelUrl, idempotencyKey);

            payment.updatePaymentStatus(PaymentStatus.CANCELED);

            return PaymentCancelResponseDto.builder()
                    .paymentKey(paymentCancelResponseDto.getPaymentKey())
                    .orderId(paymentCancelResponseDto.getOrderId())
                    .status(paymentCancelResponseDto.getStatus())
                    .build();
        } catch (Exception e) {
            log.error("결제 취소 처리 중 오류 발생: {}", e.getMessage());
            throw e;
        } finally {
            deleteIdempotencyKey(key);
        }
    }

    @Retryable(
            retryFor = { WebClientResponseException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    private PaymentApproveResponseDto sendApprovalRequestToToss(PaymentApproveRequestDto paymentApproveRequestDto, String url, String idempotencyKey) {
        String authorizationHeader = "Basic " + Base64.getEncoder().encodeToString((tossApiKey + ":").getBytes());

        try {
            return webClient.post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .header("Idempotency-Key", idempotencyKey)
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

    @Recover
    public void recover(WebClientResponseException ex, PaymentApproveRequestDto requestDto) {
        log.error("결제 승인 재시도 실패: 요청 ID={}, 오류={}", requestDto.getOrderId(), ex.getMessage());
    }

    @Retryable(
            retryFor = { WebClientResponseException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    private PaymentCancelResponseDto sendCancelRequestToToss(PaymentCancelRequestDto paymentCancelRequestDto, String url, String idempotencyKey) {
        String authorizationHeader = "Basic " + Base64.getEncoder().encodeToString((tossApiKey + ":").getBytes());

        try {
            return webClient.post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .header("Idempotency-Key", idempotencyKey)
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

    @Recover
    public void recover(WebClientResponseException ex, PaymentCancelRequestDto requestDto) {
        log.error("결제 취소 재시도 실패: 요청 ID={}, 오류={}", requestDto.getPaymentKey(), ex.getMessage());
    }

    private void validatePaymentAmount(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("결제 금액은 0보다 커야 합니다.");
        }
    }

    private boolean saveIdempotencyKey(String key, String value) {
        Boolean isSaved = redisTemplate.opsForValue().setIfAbsent(key, value, Duration.ofMillis(IDEMPOTENCY_KEY_TTL));
        return !Boolean.TRUE.equals(isSaved);
    }

    private void deleteIdempotencyKey(String key) {
        redisTemplate.delete(key);
    }

}
