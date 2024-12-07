package com.dangdangsalon.domain.estimate.service;

import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.entity.EstimateStatus;
import com.dangdangsalon.domain.estimate.repository.EstimateRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class EstimateNotificationServiceTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EstimateRepository estimateRepository;

    @Mock
    private RedisNotificationService redisNotificationService;

    @InjectMocks
    private EstimateNotificationService estimateNotificationService;

    private EstimateRequest mockEstimateRequest;
    private Estimate mockEstimate;
    private GroomerProfile mockGroomerProfile;
    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockUser = User.builder()
                .notificationEnabled(true)
                .build();
        ReflectionTestUtils.setField(mockUser, "id", 1L);

        mockEstimateRequest = EstimateRequest.builder()
                .user(mockUser)
                .build();

        mockEstimate = Estimate.builder()
                .estimateRequest(mockEstimateRequest)
                .status(EstimateStatus.SEND)
                .build();
        ReflectionTestUtils.setField(mockEstimate, "id", 1L);

        mockGroomerProfile = GroomerProfile.builder()
                .name("테스트 미용사")
                .build();

    }

    @Test
    @DisplayName("알림 전송 - 성공")
    void sendNotificationToUser_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(notificationService.getFcmToken(1L)).thenReturn(Optional.of("dummyFcmToken"));

        // When
        estimateNotificationService.sendNotificationToUser(mockEstimateRequest, mockEstimate, mockGroomerProfile);

        // Then
        verify(notificationService, times(1)).sendNotificationWithData(
                eq("dummyFcmToken"),
                eq("테스트 미용사님이 견적을 보냈습니다."),
                eq("견적 내용을 확인해보세요."),
                eq("견적서"),
                eq(1L)
        );
        verify(redisNotificationService, times(1)).saveNotificationToRedis(
                eq(1L),
                eq("테스트 미용사님이 견적을 보냈습니다."),
                eq("견적 내용을 확인해보세요."),
                eq("견적서"),
                eq(1L)
        );
    }

    @Test
    @DisplayName("알림 전송 - 알림 비활성화")
    void sendNotificationToUser_NotificationDisabled() {
        // Given
        mockUser.updateNotificationEnabled(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // When
        estimateNotificationService.sendNotificationToUser(mockEstimateRequest, mockEstimate, mockGroomerProfile);

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
                estimateNotificationService.sendNotificationToUser(mockEstimateRequest, mockEstimate, mockGroomerProfile)
        );
    }

    @Test
    @DisplayName("견적 상태 업데이트 - 성공")
    void updateEstimateStatus_Success() {
        // Given
        when(estimateRepository.findById(1L)).thenReturn(Optional.of(mockEstimate));

        // When
        estimateNotificationService.updateEstimateStatus(1L);

        // Then
        assertThat(mockEstimate.getStatus()).isEqualTo(EstimateStatus.ACCEPTED);
        verify(notificationService, times(1)).scheduleReviewNotification(1L, 1L);
    }

    @Test
    @DisplayName("견적 상태 업데이트 - 견적 없음 예외")
    void updateEstimateStatus_EstimateNotFound() {
        // Given
        when(estimateRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> estimateNotificationService.updateEstimateStatus(1L));
    }
}
