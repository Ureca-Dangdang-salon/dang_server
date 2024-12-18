package com.dangdangsalon.domain.payment.service;

import com.dangdangsalon.domain.payment.dto.PaymentApproveRequestDto;
import com.dangdangsalon.domain.payment.dto.PaymentApproveResponseDto;
import com.dangdangsalon.domain.payment.dto.PaymentCancelRequestDto;
import com.dangdangsalon.domain.payment.dto.PaymentCancelResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentApproveService paymentApproveService;
    private final PaymentCancelService paymentCancelService;
    private final RedisTemplate<String, String> redisTemplate;

    private static final long IDEMPOTENCY_KEY_TTL = 10 * 60 * 1000L; // 10분 설정
    private static final String PAYMENT_IDEMPOTENCY_KEY_PREFIX = "payment:approve:idempotency:";
    private static final String CANCEL_IDEMPOTENCY_KEY_PREFIX = "payment:cancel:idempotency:";

    @Transactional
    public PaymentApproveResponseDto approvePayment(PaymentApproveRequestDto paymentApproveRequestDto, Long userId) {
        String idempotencyKey = UUID.randomUUID().toString();
        String key = PAYMENT_IDEMPOTENCY_KEY_PREFIX + userId;

        if (saveIdempotencyKey(key, idempotencyKey)) {
            throw new IllegalStateException("이미 동일한 결제 승인 요청이 처리 중입니다.");
        }

        try {
            return paymentApproveService.processPaymentApproval(
                    paymentApproveRequestDto,
                    idempotencyKey
            );
        } catch (Exception e) {
            log.error("결제 승인 처리 중 오류 발생: {}", e.getMessage());
            throw e;
        } finally {
            deleteIdempotencyKey(key);
        }
    }

    @Transactional
    public PaymentCancelResponseDto cancelPayment(PaymentCancelRequestDto paymentCancelRequestDto, Long userId) {
        String idempotencyKey = UUID.randomUUID().toString();
        String key = CANCEL_IDEMPOTENCY_KEY_PREFIX + userId;

        if (saveIdempotencyKey(key, idempotencyKey)) {
            throw new IllegalStateException("이미 동일한 결제 취소 요청이 처리 중입니다.");
        }

        try {
            return paymentCancelService.processPaymentCancellation(
                    paymentCancelRequestDto,
                    idempotencyKey
            );
        } catch (Exception e) {
            log.error("결제 취소 처리 중 오류 발생: {}", e.getMessage());
            throw e;
        } finally {
            deleteIdempotencyKey(key);
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