package com.dangdangsalon.domain.coupon.scheduler;

import com.dangdangsalon.domain.coupon.entity.CouponEvent;
import com.dangdangsalon.domain.coupon.service.CouponIssueService;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CouponIssueService couponIssueService;
    private final TaskScheduler taskScheduler;
    private ScheduledFuture<?> scheduledTask;

//    @Value("${coupon.event.id}")
//    private Long eventId;

    private static final int BATCH_SIZE = 30;
//    private static final Long EVENT_ID = 1L;

    // 발급 스케줄러 시작
    public void startDynamicScheduler(Long eventId) {
        if (scheduledTask == null || scheduledTask.isCancelled()) {
            Duration interval = Duration.ofSeconds(10);
            scheduledTask = taskScheduler.scheduleAtFixedRate(() -> processQueueEvent(eventId), interval);
            log.info("동적 스케줄러가 시작되었습니다.");
        }
    }

    // 발급 스케줄러 중지
    public void stopDynamicScheduler() {
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            log.info("동적 스케줄러가 중지되었습니다.");
        }
    }

    // 발급 처리 함수
    protected void processQueueEvent(Long eventId) {
        String queueKey = "coupon_queue:" + eventId;
        String couponRemainingKey = "coupon_remaining:" + eventId;

        Integer remainingCoupons = (Integer) redisTemplate.opsForValue().get(couponRemainingKey);

        if (remainingCoupons == null || remainingCoupons <= 0) {
            log.info("남은 쿠폰이 없어 대기열 처리를 종료합니다.");
            stopDynamicScheduler();
            return;
        }

        for (int i = 0; i < BATCH_SIZE; i++) {
            Set<Object> firstUserSet = redisTemplate.opsForZSet().range(queueKey, 0, 0);

            if (firstUserSet == null || firstUserSet.isEmpty()) {
                break;
            }

            couponIssueService.processQueue(eventId);
        }
    }

    // 현재 시간이 startedAt 보다 빠를 경우 실행하여 스케줄러 실행
    public void scheduleEvent(CouponEvent couponEvent) {
        Instant startInstant = couponEvent.getStartedAt().atZone(ZoneId.systemDefault()).toInstant();

        // startedAt에서 3분 전 시간 계산
        LocalDateTime executionTime = couponEvent.getStartedAt().minusMinutes(3);
        Instant settingInstant = executionTime.atZone(ZoneId.systemDefault()).toInstant();

        // 스케줄러 등록
        taskScheduler.schedule(() -> startEvent(couponEvent), settingInstant);

        taskScheduler.schedule(() -> startDynamicScheduler(couponEvent.getId()), startInstant);

        log.info("이벤트 ID {}의 작업이 {}에 스케줄링되었습니다.", couponEvent.getId(), executionTime);
    }

    public void startEvent(CouponEvent couponEvent) {
        Long eventId = couponEvent.getId();

        String couponRemainingKey = "coupon_remaining:" + eventId;
        String couponQueueKey = "coupon_queue:" + eventId;
        String issuedUsersKey = "issued_users:" + eventId;

        // Redis 초기화
        redisTemplate.opsForValue().set(couponRemainingKey, couponEvent.getTotalQuantity());
        redisTemplate.delete(couponQueueKey);
        redisTemplate.delete(issuedUsersKey);

        log.info("이벤트 시작 3분 전 - ID: {}, 쿠폰 수량: {}", eventId, couponEvent.getTotalQuantity());
    }

}
