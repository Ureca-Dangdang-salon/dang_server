package com.dangdangsalon.domain.coupon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventSetupRunner implements CommandLineRunner {

    private final CouponEventInitService couponEventInitService;

    @Override
    public void run(String... args) {
        couponEventInitService.initializeEvent("TestEvent", 2000);
    }
}