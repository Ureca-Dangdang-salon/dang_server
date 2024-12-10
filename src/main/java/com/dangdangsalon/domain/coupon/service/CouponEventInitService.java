package com.dangdangsalon.domain.coupon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class CouponEventInitService {

    private static final String REMAINING_KEY_FORMAT = "coupon:event:%s:remaining";
    private static final String QUEUE_KEY_FORMAT = "coupon:event:%s:queue";
    private static final String ISSUED_KEY_FORMAT = "coupon:event:%s:issued";

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 이벤트 초기화
     * @param eventName 이벤트 이름
     * @param totalQuantity 총 쿠폰 수량
     */
    public void initializeEvent(String eventName, int totalQuantity) {
        String remainingKey = String.format(REMAINING_KEY_FORMAT, eventName);
        String queueKey = String.format(QUEUE_KEY_FORMAT, eventName);
        String issuedKey = String.format(ISSUED_KEY_FORMAT, eventName);

        // 남은 쿠폰 수량 초기화
        redisTemplate.opsForValue().set(remainingKey, totalQuantity);
        redisTemplate.expire(remainingKey, Duration.ofDays(7)); // 7일 만료

        // 대기열 초기화
        redisTemplate.delete(queueKey); // 기존 대기열 삭제
        redisTemplate.expire(queueKey, Duration.ofDays(7)); // 7일 만료

        // 발급 기록 초기화
        redisTemplate.delete(issuedKey); // 기존 발급 기록 삭제

        System.out.printf("이벤트 '%s' 초기화 완료: 총 쿠폰 수량 %d%n", eventName, totalQuantity);
    }
}