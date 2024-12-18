package com.dangdangsalon.domain.coupon.service;

import com.dangdangsalon.domain.coupon.entity.CouponEvent;
import com.dangdangsalon.domain.coupon.repository.CouponEventRepository;
import com.dangdangsalon.domain.coupon.scheduler.CouponScheduler;
import org.springframework.stereotype.Service;

import java.util.List;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CouponEventService {

    private final CouponEventRepository couponEventRepository;
    private final CouponScheduler couponScheduler;

    public void scheduleUpcomingEvents() {
        // DB에서 startedAt 기준으로 현재 시간 이후의 이벤트 조회
        List<CouponEvent> upcomingEvents = couponEventRepository.findUpcomingEvents();

        // 각 이벤트 스케줄링
        for (CouponEvent event : upcomingEvents) {
            couponScheduler.scheduleEvent(event);
        }
    }
}
