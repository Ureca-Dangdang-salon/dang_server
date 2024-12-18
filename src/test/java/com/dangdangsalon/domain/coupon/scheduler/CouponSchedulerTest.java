package com.dangdangsalon.domain.coupon.scheduler;

import com.dangdangsalon.domain.coupon.entity.CouponEvent;
import com.dangdangsalon.domain.coupon.service.CouponIssueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import static org.mockito.Mockito.*;

@DisplayName("CouponScheduler Test")
@ExtendWith(MockitoExtension.class)
class CouponSchedulerTest {

    @InjectMocks
    private CouponScheduler couponScheduler;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private CouponIssueService couponIssueService;

    @Mock
    private TaskScheduler taskScheduler;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private ZSetOperations<String, Object> zSetOperations;

    private static final String QUEUE_KEY = "coupon_queue:1";
    private static final String REMAINING_KEY = "coupon_remaining:1";
    private static final int BATCH_SIZE = 30;
    private static final Long EVENT_ID = 1L;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }

    @Test
    @DisplayName("동적 스케줄러 시작 테스트")
    void startDynamicSchedulerTest() {
        // Given
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);

        when(taskScheduler.scheduleAtFixedRate(runnableCaptor.capture(), durationCaptor.capture()))
                .thenReturn(mock(ScheduledFuture.class));

        // When
        couponScheduler.startDynamicScheduler(EVENT_ID);

        // Then
        verify(taskScheduler, times(1)).scheduleAtFixedRate(any(Runnable.class), any(Duration.class));
    }

    @Test
    @DisplayName("동적 스케줄러 정지 테스트")
    void stopDynamicSchedulerTest() {
        // Given
        ScheduledFuture<?> mockFuture = mock(ScheduledFuture.class); // ScheduledFuture 모의 객체 생성
        when(taskScheduler.scheduleAtFixedRate(any(Runnable.class), any(Duration.class)))
                .thenAnswer(invocation -> {
                    // 스케줄러의 반환값을 모의 객체로 설정
                    ReflectionTestUtils.setField(couponScheduler, "scheduledTask", mockFuture);
                    return mockFuture;
                });

        couponScheduler.startDynamicScheduler(EVENT_ID); // 동적 스케줄러 시작

        // When
        couponScheduler.stopDynamicScheduler();

        // Then
        verify(mockFuture, times(1)).cancel(false);
    }

    @Test
    @DisplayName("남아 있는 쿠폰 Queue 처리 테스트")
    void processQueueEvent_withRemainingCoupons() {
        // Given
        when(valueOperations.get(REMAINING_KEY)).thenReturn(10);
        when(zSetOperations.range(QUEUE_KEY, 0, 0)).thenReturn(Set.of("user1"));

        // When
        couponScheduler.processQueueEvent(EVENT_ID);

        // Then
        verify(couponIssueService, times(BATCH_SIZE)).processQueue(EVENT_ID);
    }

    @Test
    @DisplayName("남은 쿠폰이 없는 경우 Queue 처리 테스트")
    void processQueueEvent_noRemainingCoupons() {
        // Given
        when(valueOperations.get(REMAINING_KEY)).thenReturn(0);

        // When
        couponScheduler.processQueueEvent(EVENT_ID);

        // Then
        verify(couponIssueService, never()).processQueue(EVENT_ID);
    }

    @Test
    @DisplayName("Queue가 비어 있는 경우 처리 테스트")
    void processQueueEvent_emptyQueue() {
        // Given
        when(valueOperations.get(REMAINING_KEY)).thenReturn(10);
        when(zSetOperations.range(QUEUE_KEY, 0, 0)).thenReturn(Set.of());

        // When
        couponScheduler.processQueueEvent(EVENT_ID);

        // Then
        verify(couponIssueService, never()).processQueue(anyLong());
    }

    @Test
    @DisplayName("이벤트 스케줄링 테스트")
    void scheduleEventTest() {
        // Given
        CouponEvent couponEvent = mock(CouponEvent.class);
        LocalDateTime startAt = LocalDateTime.now().plusMinutes(5);
        when(couponEvent.getId()).thenReturn(EVENT_ID);
        when(couponEvent.getStartedAt()).thenReturn(startAt);

        // When
        couponScheduler.scheduleEvent(couponEvent);

        // Then
        verify(taskScheduler, times(2)).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    @DisplayName("이벤트 시작 테스트")
    void startEventTest() {
        // Given
        CouponEvent couponEvent = mock(CouponEvent.class);
        when(couponEvent.getId()).thenReturn(EVENT_ID);
        when(couponEvent.getTotalQuantity()).thenReturn(100);

        // When
        couponScheduler.startEvent(couponEvent);

        // Then
        verify(redisTemplate.opsForValue(), times(1)).set("coupon_remaining:" + EVENT_ID, 100);
        verify(redisTemplate, times(1)).delete("coupon_queue:" + EVENT_ID);
        verify(redisTemplate, times(1)).delete("issued_users:" + EVENT_ID);
    }
}
