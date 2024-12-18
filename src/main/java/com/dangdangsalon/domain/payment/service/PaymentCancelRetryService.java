package com.dangdangsalon.domain.payment.service;

import com.dangdangsalon.domain.payment.dto.PaymentCancelRequestDto;
import com.dangdangsalon.domain.payment.dto.PaymentCancelResponseDto;
import com.dangdangsalon.domain.payment.entity.Payment;
import com.dangdangsalon.domain.payment.entity.PaymentStatus;
import com.dangdangsalon.domain.payment.repository.PaymentRepository;
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

import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCancelRetryService {

    private final WebClient webClient;
    private final PaymentRepository paymentRepository;

    @Value("${toss.api.key}")
    private String tossApiKey;

    @Retryable(
            retryFor = { WebClientResponseException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public PaymentCancelResponseDto sendCancelRequestToToss(PaymentCancelRequestDto paymentCancelRequestDto, String url, String idempotencyKey) {

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
            throw ex;
        }
    }

    @Recover
    public PaymentCancelResponseDto recover(WebClientResponseException ex, PaymentCancelRequestDto requestDto) {
        log.error("결제 취소 재시도 실패: 요청 ID={}, 오류={}", requestDto.getPaymentKey(), ex.getMessage());

        Payment payment = paymentRepository.findByPaymentKey(requestDto.getPaymentKey())
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        payment.updatePaymentStatus(PaymentStatus.REJECTED);

        return PaymentCancelResponseDto.builder()
                .status(payment.getPaymentStatus().name())
                .build();
    }
}
