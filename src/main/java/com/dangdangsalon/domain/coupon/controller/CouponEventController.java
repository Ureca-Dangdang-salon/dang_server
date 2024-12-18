package com.dangdangsalon.domain.coupon.controller;

import com.dangdangsalon.domain.coupon.entity.CouponEvent;
import com.dangdangsalon.domain.coupon.repository.CouponEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/couponevent")
@Slf4j
public class CouponEventController {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CouponEventRepository couponEventRepository;

    @PostMapping("/start")
    public String startEvent(@RequestParam Long eventId) {
        CouponEvent event = couponEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("해당 이벤트를 찾을 수 없습니다. Id: " + eventId));

        String couponRemainingKey = "coupon_remaining:" + eventId;
        String couponQueueKey = "coupon_queue:" + eventId;
        String issuedUsersKey = "issued_users:" + eventId;

        redisTemplate.opsForValue().set(couponRemainingKey, event.getTotalQuantity());
        redisTemplate.delete(couponQueueKey);
        redisTemplate.delete(issuedUsersKey);

        log.info("이벤트 시작 - ID: {}, 쿠폰 수량: {}", eventId, event.getTotalQuantity());
        return "이벤트가 시작되었습니다. ID: " + eventId;
    }
}
