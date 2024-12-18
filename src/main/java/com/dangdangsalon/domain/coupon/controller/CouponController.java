package com.dangdangsalon.domain.coupon.controller;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.coupon.dto.CouponInfoResponseDto;
import com.dangdangsalon.domain.coupon.dto.CouponMainResponseDto;
import com.dangdangsalon.domain.coupon.dto.CouponUserResponseDto;
import com.dangdangsalon.domain.coupon.service.CouponIssueService;
import com.dangdangsalon.domain.coupon.service.CouponService;
import com.dangdangsalon.util.ApiUtil;
import com.dangdangsalon.util.ApiUtil.ApiSuccess;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coupons")
public class CouponController {

    private final CouponIssueService couponIssueService;
    private final CouponService couponService;

    @PostMapping("/issued")
    public ApiSuccess<?> issueCoupon(@AuthenticationPrincipal CustomOAuth2User user, @RequestParam Long eventId) {
        Long userId = user.getUserId();
        String result = couponIssueService.joinQueue(userId, eventId);

        return ApiUtil.success(result);
    }

    /*
     SSE 구현 시 사용되는 MIME 타입 (TEXT_EVENT_STREAM_VALUE)
     브라우저와 클라이언트는 text/event-stream 타입을 인식하고 실시간 데이터 스트림으로 처리
     React에서는 EventSource 객체가 이 스트림을 처리한다.
     */
    @GetMapping(value = "/queue/updates", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeQueueUpdates(@AuthenticationPrincipal CustomOAuth2User user, @RequestParam Long eventId) {
        Long userId = user.getUserId();
        return couponIssueService.subscribeQueueUpdates(userId, eventId);
    }

    @GetMapping("/main")
    public ApiSuccess<?> getCouponValidMainPage() {
        List<CouponMainResponseDto> result = couponService.getCouponValidMainPage();
        return ApiUtil.success(result);
    }

    @GetMapping("/{eventId}")
    public ApiSuccess<?> getCouponInfo(@PathVariable Long eventId) {
        CouponInfoResponseDto result = couponService.getCouponInfo(eventId);
        return ApiUtil.success(result);
    }

    @GetMapping("/users")
    public ApiSuccess<?> getUserCoupon(@AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();
        List<CouponUserResponseDto> result = couponService.getUserCoupon(userId);
        return ApiUtil.success(result);
    }
}
