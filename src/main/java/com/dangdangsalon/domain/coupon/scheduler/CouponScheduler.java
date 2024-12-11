package com.dangdangsalon.domain.coupon.scheduler;

import com.dangdangsalon.domain.coupon.service.CouponService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CouponService couponService;

//    @Value("${coupon.event.id}")
//    private Long eventId;

    @Scheduled(fixedRate = 2000)
    public void processQueueEvent() {
        String queueKey = "coupon_queue:" + 1L;
        String couponRemainingKey = "coupon_remaining:" + 1L;

        Integer remainingCoupons = (Integer) redisTemplate.opsForValue().get(couponRemainingKey);

        if (remainingCoupons == null || remainingCoupons <= 0) {
            log.info("남은 쿠폰이 없어 대기열 처리를 종료합니다.");
            return;
        }

        Set<Object> firstUserSet = redisTemplate.opsForZSet().range(queueKey, 0, 0);

        if (firstUserSet != null && !firstUserSet.isEmpty()) {
            String userIdString = (String) firstUserSet.iterator().next();
            Long userId = Long.valueOf(userIdString);

            log.info("대기열의 첫 번째 사용자 처리 중: userId={}", userId);

            couponService.processQueue(1L);
        } else {
            log.info("대기열이 비어 있어 처리할 작업이 없습니다.");
        }
    }
}
