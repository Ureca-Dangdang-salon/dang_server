package com.dangdangsalon.domain.coupon.controller;

import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CouponTestController {

    @GetMapping("/test/coupon")
    public String testCouponPage() {
        return "coupontest";
    }
}
