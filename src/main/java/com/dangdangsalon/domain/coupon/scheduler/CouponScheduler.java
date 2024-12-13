package com.dangdangsalon.domain.coupon.scheduler;

import com.dangdangsalon.domain.coupon.service.CouponIssueService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CouponIssueService couponIssueService;
//    @Value("${coupon.event.id}")
//    private Long eventId;

    private static final int BATCH_SIZE = 100;
    private static final Long EVENT_ID = 1L;

    @Scheduled(fixedRate = 10000) // 10초
    public void processQueueEvent() {
        String queueKey = "coupon_queue:" + EVENT_ID;
        String couponRemainingKey = "coupon_remaining:" + EVENT_ID;

        Integer remainingCoupons = (Integer) redisTemplate.opsForValue().get(couponRemainingKey);

        if (remainingCoupons == null || remainingCoupons <= 0) {
            log.info("남은 쿠폰이 없어 대기열 처리를 종료합니다.");
            return;
        }

        for (int i = 0; i < BATCH_SIZE; i++) {
            Set<Object> firstUserSet = redisTemplate.opsForZSet().range(queueKey, 0, 0);

            if (firstUserSet == null || firstUserSet.isEmpty()) {
                break;
            }

            couponIssueService.processQueue(EVENT_ID);
        }
    }

    @Scheduled(cron = "0 0 0 15 * ?") // 매월 15일 자정에 실행
    public void startEventAutomatically() {
        couponIssueService.initializeEvents();
    }
}
