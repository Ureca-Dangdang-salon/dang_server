package com.dangdangsalon.domain.estimate.request.service;

import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.notification.service.NotificationService;
import com.dangdangsalon.domain.notification.service.RedisNotificationService;
import com.dangdangsalon.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class GroomerEstimateRequestNotificationServiceTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private RedisNotificationService redisNotificationService;

    @InjectMocks
    private GroomerEstimateRequestNotificationService groomerEstimateRequestNotificationService;

    private GroomerProfile mockGroomerProfile;
    private User mockUser;
    private EstimateRequest mockEstimateRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock 객체 생성
        mockUser = User.builder()
                .notificationEnabled(true) // 알림 활성화
                .build();

        mockGroomerProfile = GroomerProfile.builder()
                .name("테스트 미용사")
                .build();

        mockEstimateRequest = EstimateRequest.builder().build();

        // Reflection으로 ID 값 설정
        ReflectionTestUtils.setField(mockUser, "id", 1L); // User ID 설정
        ReflectionTestUtils.setField(mockGroomerProfile, "user", mockUser); // GroomerProfile에 User 설정
        ReflectionTestUtils.setField(mockEstimateRequest, "id", 2L); // EstimateRequest ID 설정
    }

    @Test
    @DisplayName("알림 전송 - 성공")
    void sendNotificationToGroomer_Success() {
        // Given
        List<String> fcmTokens = Arrays.asList("token1", "token2");
        when(notificationService.getFcmTokens(1L)).thenReturn(fcmTokens);

        for (String token : fcmTokens) {
            when(notificationService.sendNotificationWithData(
                    eq(token),
                    eq("새로운 견적 요청"),
                    eq("새로운 견적 요청이 도착했습니다. 확인하세요."),
                    eq("견적 요청"),
                    eq(2L)
            )).thenReturn(true);
        }

        // When
        groomerEstimateRequestNotificationService.sendNotificationToGroomer(mockEstimateRequest, mockGroomerProfile);

        // Then
        for (String token : fcmTokens) {
            verify(notificationService, times(1)).sendNotificationWithData(
                    eq(token),
                    eq("새로운 견적 요청"),
                    eq("새로운 견적 요청이 도착했습니다. 확인하세요."),
                    eq("견적 요청"),
                    eq(2L)
            );
        }

        verify(redisNotificationService, times(1)).saveNotificationToRedis(
                eq(1L),
                eq("새로운 견적 요청"),
                eq("새로운 견적 요청이 도착했습니다. 확인하세요."),
                eq("견적 요청"),
                eq(2L)
        );
    }

    @Test
    @DisplayName("알림 전송 - FCM 실패로 Redis 저장 안됨")
    void sendNotificationToGroomer_FcmSendFailed() {
        // Given
        List<String> fcmTokens = Collections.singletonList("token1");
        when(notificationService.getFcmTokens(1L)).thenReturn(fcmTokens);

        when(notificationService.sendNotificationWithData(
                eq("token1"),
                eq("새로운 견적 요청"),
                eq("새로운 견적 요청이 도착했습니다. 확인하세요."),
                eq("견적 요청"),
                eq(2L)
        )).thenReturn(false); // 실패 시 false 반환

        // When
        groomerEstimateRequestNotificationService.sendNotificationToGroomer(mockEstimateRequest, mockGroomerProfile);

        // Then
        verify(notificationService, times(1)).sendNotificationWithData(
                eq("token1"),
                eq("새로운 견적 요청"),
                eq("새로운 견적 요청이 도착했습니다. 확인하세요."),
                eq("견적 요청"),
                eq(2L)
        );
        verify(redisNotificationService, never()).saveNotificationToRedis(
                anyLong(),
                anyString(),
                anyString(),
                anyString(),
                anyLong()
        );
    }

    @Test
    @DisplayName("알림 전송 - FCM 토큰 없음")
    void sendNotificationToGroomer_FcmTokenNotFound() {
        // Given
        when(notificationService.getFcmTokens(1L)).thenReturn(Collections.emptyList()); // FCM 토큰 없음

        // When
        groomerEstimateRequestNotificationService.sendNotificationToGroomer(mockEstimateRequest, mockGroomerProfile);

        // Then
        verify(notificationService, never()).sendNotificationWithData(any(), any(), any(), any(), any());
        verify(redisNotificationService, never()).saveNotificationToRedis(anyLong(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("알림 전송 - 사용자 없음 예외")
    void sendNotificationToGroomer_UserNotFound() {
        // Given
        when(notificationService.getFcmTokens(1L)).thenThrow(new IllegalArgumentException("사용자를 찾을 수 없습니다: 1"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                groomerEstimateRequestNotificationService.sendNotificationToGroomer(mockEstimateRequest, mockGroomerProfile)
        );

        verify(notificationService, never()).sendNotificationWithData(any(), any(), any(), any(), any());
        verify(redisNotificationService, never()).saveNotificationToRedis(anyLong(), any(), any(), any(), any());
    }
}
