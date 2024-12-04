package com.dangdangsalon.domain.payment.service;

import com.dangdangsalon.domain.notification.service.NotificationService;
import com.dangdangsalon.domain.notification.service.RedisNotificationService;
import com.dangdangsalon.domain.orders.entity.Orders;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@DisplayName("PaymentNotificationService 테스트")
class PaymentNotificationServiceTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisNotificationService redisNotificationService;

    @InjectMocks
    private PaymentNotificationService paymentNotificationService;

    private User mockUser;
    private Orders mockOrders;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockUser = User.builder()
                .notificationEnabled(true)
                .build();

        mockOrders = Orders.builder()
                .user(mockUser)
                .build();

        ReflectionTestUtils.setField(mockUser, "id", 1L);
        ReflectionTestUtils.setField(mockOrders, "id", 101L);
    }

    @Test
    @DisplayName("알림 전송 - 성공")
    void sendNotificationToUser_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(notificationService.getFcmToken(1L)).thenReturn(Optional.of("dummyFcmToken"));

        // When
        paymentNotificationService.sendNotificationToUser(mockOrders);

        // Then
        verify(notificationService, times(1)).sendNotificationWithData(
                eq("dummyFcmToken"),
                eq("결제가 완료되었습니다"),
                eq("결제 내역을 확인해보세요."),
                eq("결제"),
                eq(1L)
        );
        verify(redisNotificationService, times(1)).saveNotificationToRedis(
                eq(1L),
                eq("결제가 완료되었습니다"),
                eq("결제 내역을 확인해보세요."),
                eq("결제"),
                eq(1L)
        );
    }

    @Test
    @DisplayName("알림 전송 - 알림 비활성화")
    void sendNotificationToUser_NotificationDisabled() {
        // Given
        mockUser.updateNotificationEnabled(false); // 알림 비활성화
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // When
        paymentNotificationService.sendNotificationToUser(mockOrders);

        // Then
        verify(notificationService, never()).getFcmToken(anyLong());
        verify(notificationService, never()).sendNotificationWithData(any(), any(), any(), any(), any());
        verify(redisNotificationService, never()).saveNotificationToRedis(anyLong(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("알림 전송 - 사용자 없음 예외")
    void sendNotificationToUser_UserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                paymentNotificationService.sendNotificationToUser(mockOrders)
        );

        verify(notificationService, never()).getFcmToken(anyLong());
        verify(notificationService, never()).sendNotificationWithData(any(), any(), any(), any(), any());
        verify(redisNotificationService, never()).saveNotificationToRedis(anyLong(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("알림 전송 - FCM 토큰 없음")
    void sendNotificationToUser_NoFcmToken() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(notificationService.getFcmToken(1L)).thenReturn(Optional.empty());

        // When
        paymentNotificationService.sendNotificationToUser(mockOrders);

        // Then
        verify(notificationService, times(1)).getFcmToken(1L);
        verify(notificationService, never()).sendNotificationWithData(any(), any(), any(), any(), any());
        verify(redisNotificationService, never()).saveNotificationToRedis(anyLong(), any(), any(), any(), any());
    }
}
