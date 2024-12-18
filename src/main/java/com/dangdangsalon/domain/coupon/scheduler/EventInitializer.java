package com.dangdangsalon.domain.coupon.scheduler;

import javax.annotation.PostConstruct;

import com.dangdangsalon.domain.coupon.service.CouponEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventInitializer {

    private final CouponEventService couponEventService;

    @PostConstruct
    public void initializeScheduledEvents() {
        couponEventService.scheduleUpcomingEvents();
    }
}