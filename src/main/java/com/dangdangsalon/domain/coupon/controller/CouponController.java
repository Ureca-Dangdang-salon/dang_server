package com.dangdangsalon.domain.coupon.controller;

import com.dangdangsalon.domain.coupon.service.CouponService;
import com.dangdangsalon.util.ApiUtil;
import com.dangdangsalon.util.ApiUtil.ApiSuccess;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coupons")
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/issued")
    public ApiSuccess<?> issueCoupon(@RequestParam Long userId, @RequestParam Long eventId) {
        couponService.joinQueue(userId, eventId);
        return ApiUtil.success("대기열에 성공적으로 참여했습니다.");
    }

    /*
     SSE 구현 시 사용되는 MIME 타입 (TEXT_EVENT_STREAM_VALUE)
     브라우저와 클라이언트는 text/event-stream 타입을 인식하고 실시간 데이터 스트림으로 처리
     React에서는 EventSource 객체가 이 스트림을 처리한다.
     */
    @GetMapping(value = "/queue/updates", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeQueueUpdates() {
        return couponService.subscribeQueueUpdates();
    }
}
