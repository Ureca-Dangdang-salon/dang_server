package com.dangdangsalon.domain.payment.service;

import com.dangdangsalon.domain.payment.dto.PaymentApproveRequestDto;
import com.dangdangsalon.domain.payment.dto.PaymentApproveResponseDto;
import com.dangdangsalon.domain.payment.entity.Payment;
import com.dangdangsalon.domain.payment.entity.PaymentStatus;
import com.dangdangsalon.domain.payment.repository.PaymentRepository;
import com.dangdangsalon.domain.orders.entity.Orders;
import com.dangdangsalon.domain.orders.repository.OrdersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentApproveRetryService {

    private final WebClient webClient;
    private final OrdersRepository ordersRepository;
    private final PaymentRepository paymentRepository;

    @Value("${toss.api.key}")
    private String tossApiKey;

    @Retryable(
            retryFor = {WebClientResponseException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public PaymentApproveResponseDto sendApprovalRequestToToss(PaymentApproveRequestDto paymentApproveRequestDto, String url, String idempotencyKey) {
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
            throw ex;
        }
    }

    @Recover
    public PaymentApproveResponseDto recover(WebClientResponseException ex, PaymentApproveRequestDto requestDto) {
        log.error("결제 승인 재시도 실패: 요청 ID={}, 오류={}", requestDto.getOrderId(), ex.getMessage());

        Orders order = ordersRepository.findByTossOrderId(requestDto.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("주문 정보를 찾을 수 없습니다: " + requestDto.getOrderId()));

        Payment rejectedPayment = Payment.builder()
                .paymentKey(requestDto.getPaymentKey())
                .totalAmount(0)
                .paymentStatus(PaymentStatus.REJECTED)
                .requestedAt(LocalDateTime.now())
                .paymentMethod("UNKNOWN")
                .orders(order)
                .coupon(null)
                .build();

        paymentRepository.save(rejectedPayment);

        return PaymentApproveResponseDto.builder()
                .status(rejectedPayment.getPaymentStatus().name())
                .build();
    }
}
