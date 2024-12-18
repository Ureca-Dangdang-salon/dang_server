package com.dangdangsalon.domain.coupon.service;

import com.dangdangsalon.domain.coupon.entity.CouponEvent;
import com.dangdangsalon.domain.coupon.repository.CouponEventRepository;
import com.dangdangsalon.domain.notification.dto.EventNotificationDto;
import com.dangdangsalon.domain.notification.repository.FcmTokenRepository;
import com.dangdangsalon.domain.notification.service.EventNotificationProducer;
import com.dangdangsalon.domain.user.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CouponKafkaNotificationServiceTest {

    @Mock
    private CouponEventRepository couponEventRepository;

    @Mock
    private FcmTokenRepository fcmTokenRepository;

    @Mock
    private EventNotificationProducer producer;

    @InjectMocks
    private CouponKafkaNotificationService couponKafkaNotificationService;

    private CouponEvent mockEvent;

    @BeforeEach
    void setUp() {
        mockEvent = CouponEvent.builder()
                .name("Test Event")
                .startedAt(LocalDateTime.now().plusMinutes(30))
                .endedAt(LocalDateTime.now().plusHours(2))
                .build();
    }

    @Test
    @DisplayName("쿠폰 알림 전송 테스트 - 성공")
    void testSendCouponNotifications_Success() {
        // Given
        given(couponEventRepository.findFirstByStartedAtBetweenAndEndedAtAfter(any(), any(), any()))
                .willReturn(mockEvent);

        List<String> mockFcmTokens = List.of("token1", "token2");
        given(fcmTokenRepository.findAllByUserRole(any())).willReturn(mockFcmTokens);

        // When
        couponKafkaNotificationService.sendCouponNotifications();

        // Then
        verify(couponEventRepository, times(1)).findFirstByStartedAtBetweenAndEndedAtAfter(any(), any(), any());
        verify(fcmTokenRepository, times(1)).findAllByUserRole(Role.ROLE_USER);
        verify(producer, times(1)).sendEventNotification(any(EventNotificationDto.class));
    }


    @Test
    @DisplayName("쿠폰 알림 전송 테스트 - 이벤트 없음")
    void testSendCouponNotifications_NoEvent() {
        // Given
        given(couponEventRepository.findFirstByStartedAtBetweenAndEndedAtAfter(any(), any(), any()))
                .willReturn(null);

        // When
        couponKafkaNotificationService.sendCouponNotifications();

        // Then
        verify(couponEventRepository, times(1)).findFirstByStartedAtBetweenAndEndedAtAfter(any(), any(), any());
        verify(fcmTokenRepository, never()).findAllByUserRole(Role.ROLE_USER);
        verify(producer, never()).sendEventNotification(any(EventNotificationDto.class));
    }

    @Test
    @DisplayName("쿠폰 알림 전송 테스트 - 미용사 대상 없음")
    void testSendCouponNotifications_ForHairstylist_NoEvent() {
        // Given
        given(couponEventRepository.findFirstByStartedAtBetweenAndEndedAtAfter(any(), any(), any()))
                .willReturn(null);

        // When
        couponKafkaNotificationService.sendCouponNotifications();

        // Then
        verify(couponEventRepository, times(1)).findFirstByStartedAtBetweenAndEndedAtAfter(any(), any(), any());
        verify(fcmTokenRepository, never()).findAllByUserRole(Role.ROLE_SALON);
        verify(producer, never()).sendEventNotification(any(EventNotificationDto.class));
    }
}
