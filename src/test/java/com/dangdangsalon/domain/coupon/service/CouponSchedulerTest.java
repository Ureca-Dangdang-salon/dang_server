package com.dangdangsalon.domain.coupon.service;

import com.dangdangsalon.domain.coupon.scheduler.CouponScheduler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Set;

import static org.mockito.Mockito.*;

@DisplayName("CouponSchedulerTest")
@ExtendWith(MockitoExtension.class)
class CouponSchedulerTest {

    @InjectMocks
    private CouponScheduler couponScheduler;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private CouponIssueService couponIssueService;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private ZSetOperations<String, Object> zSetOperations;

    private static final String QUEUE_KEY = "coupon_queue:1";
    private static final String REMAINING_KEY = "coupon_remaining:1";
    private static final int BATCH_SIZE = 100;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }

    @Test
    @DisplayName("대기열 처리 테스트 - 남은 쿠폰 있음")
    void processQueueEvent_withRemainingCoupons() {
        // Given
        when(valueOperations.get(REMAINING_KEY)).thenReturn(10); // 10개의 쿠폰 남음
        when(zSetOperations.range(QUEUE_KEY, 0, 0)).thenReturn(Set.of("user1")); // 대기열에 사용자 있음

        // When
        couponScheduler.processQueueEvent();

        // Then
        verify(couponIssueService, times(BATCH_SIZE)).processQueue(1L); // BATCH_SIZE 만큼 처리 호출
    }

    @Test
    @DisplayName("대기열 처리 테스트 - 남은 쿠폰 없음")
    void processQueueEvent_noRemainingCoupons() {
        // Given
        when(valueOperations.get(REMAINING_KEY)).thenReturn(0); // 남은 쿠폰 없음

        // When
        couponScheduler.processQueueEvent();

        // Then
        verify(couponIssueService, never()).processQueue(1L); // 처리 호출 안 됨
    }

    @Test
    @DisplayName("대기열 처리 테스트 - 대기열 비어 있음")
    void processQueueEvent_emptyQueue() {
        // Given
        when(valueOperations.get(REMAINING_KEY)).thenReturn(10); // 10개의 쿠폰 남음
        when(zSetOperations.range(QUEUE_KEY, 0, 0)).thenReturn(Set.of()); // 대기열 비어 있음

        // When
        couponScheduler.processQueueEvent();

        // Then
        verify(couponIssueService, never()).processQueue(anyLong()); // 처리 호출 안 됨
    }

    @Test
    @DisplayName("이벤트 초기화 테스트")
    void startEventAutomatically() {
        // When
        couponScheduler.startEventAutomatically();

        // Then
        verify(couponIssueService, times(1)).initializeEvents(); // 초기화 호출
    }
}