package com.dangdangsalon.domain.coupon.scheduler;

import com.dangdangsalon.domain.coupon.service.CouponService;
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
        Long queueLength = redisTemplate.opsForList().size("coupon_queue:" + 1L);
        if (queueLength != null && queueLength > 0) {
            couponService.processQueue(1L);
        } else {
            log.info("대기열이 비어 있어 처리할 작업이 없습니다.");
        }
    }
}
