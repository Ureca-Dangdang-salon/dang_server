package com.dangdangsalon.domain.coupon.controller;

import com.dangdangsalon.domain.coupon.service.CouponEventManageService;
import com.dangdangsalon.domain.coupon.service.CouponQueueService;
import com.dangdangsalon.domain.coupon.sse.SseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CouponEventController {

    private final CouponEventManageService couponEventManageService;
    private final CouponQueueService couponQueueService;
    private final SseEmitterRegistry sseEmitterRegistry;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    // 사용자가 이벤트 대기열 등록
    @PostMapping("/coupon-events/register")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> registerQueue(@RequestParam Long userId, @RequestParam("eventName") String eventName) {
        String resultMessage = couponEventManageService.registerUserForEvent(eventName, userId);
        return ResponseEntity.ok(resultMessage);
    }

    // 싫시간 대기열
    @GetMapping(value = "/coupon-events/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam String eventName, @RequestParam Long userId) {
        // SSE 연결 등록
        SseEmitter emitter = sseEmitterRegistry.register(userId);
        executor.submit(() -> couponQueueService.streamQueueStatus(eventName, userId));
        return emitter;
    }
}
