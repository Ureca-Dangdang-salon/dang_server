package com.dangdangsalon.domain.estimate.request.service;

import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.notification.service.NotificationService;
import com.dangdangsalon.domain.notification.service.RedisNotificationService;
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

class GroomerEstimateRequestNotificationServiceTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

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
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(notificationService.getFcmToken(1L)).thenReturn(Optional.of("dummyFcmToken"));

        // When
        groomerEstimateRequestNotificationService.sendNotificationToGroomer(mockEstimateRequest, mockGroomerProfile);

        // Then
        verify(notificationService, times(1)).sendNotificationWithData(
                eq("dummyFcmToken"),
                eq("새로운 견적 요청"),
                eq("새로운 견적 요청이 도착했습니다. 확인하세요."),
                eq("견적 요청"),
                eq(2L)
        );
        verify(redisNotificationService, times(1)).saveNotificationToRedis(
                eq(1L),
                eq("새로운 견적 요청"),
                eq("새로운 견적 요청이 도착했습니다. 확인하세요."),
                eq("견적 요청"),
                eq(2L)
        );
    }

    @Test
    @DisplayName("알림 전송 - 알림 비활성화")
    void sendNotificationToGroomer_NotificationDisabled() {
        // Given
        mockUser.updateNotificationEnabled(false); // 알림 비활성화
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // When
        groomerEstimateRequestNotificationService.sendNotificationToGroomer(mockEstimateRequest, mockGroomerProfile);

        // Then
        verify(notificationService, never()).getFcmToken(anyLong());
        verify(notificationService, never()).sendNotificationWithData(any(), any(), any(), any(), any());
        verify(redisNotificationService, never()).saveNotificationToRedis(anyLong(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("알림 전송 - 사용자 없음 예외")
    void sendNotificationToGroomer_UserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                groomerEstimateRequestNotificationService.sendNotificationToGroomer(mockEstimateRequest, mockGroomerProfile)
        );

        verify(notificationService, never()).getFcmToken(anyLong());
        verify(notificationService, never()).sendNotificationWithData(any(), any(), any(), any(), any());
        verify(redisNotificationService, never()).saveNotificationToRedis(anyLong(), any(), any(), any(), any());
    }
}
