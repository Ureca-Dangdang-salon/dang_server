package com.dangdangsalon.domain.estimate.service;

import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.entity.EstimateStatus;
import com.dangdangsalon.domain.estimate.repository.EstimateRepository;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@DisplayName("EstimateNotificationService 테스트")
class EstimateNotificationServiceTest {

    @Mock
    private NotificationService notificationService;

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
        when(notificationService.getFcmToken(1L)).thenReturn(Optional.of("dummyFcmToken"));
        when(notificationService.sendNotificationWithData(
                eq("dummyFcmToken"),
                eq("테스트 미용사님이 견적을 보냈습니다."),
                eq("견적 내용을 확인해보세요."),
                eq("견적서"),
                eq(1L)
        )).thenReturn(true); // 성공 시 true 반환

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
    @DisplayName("알림 전송 - FCM 실패로 Redis 저장 안됨")
    void sendNotificationToUser_FcmSendFailed() {
        // Given
        when(notificationService.getFcmToken(1L)).thenReturn(Optional.of("dummyFcmToken"));
        when(notificationService.sendNotificationWithData(
                eq("dummyFcmToken"),
                eq("테스트 미용사님이 견적을 보냈습니다."),
                eq("견적 내용을 확인해보세요."),
                eq("견적서"),
                eq(1L)
        )).thenReturn(false); // 실패 시 false 반환

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
        verify(redisNotificationService, never()).saveNotificationToRedis(anyLong(), anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("알림 전송 - 사용자 없음 예외")
    void sendNotificationToUser_UserNotFound() {
        // Given
        when(notificationService.getFcmToken(1L)).thenReturn(Optional.empty());

        // When
        estimateNotificationService.sendNotificationToUser(mockEstimateRequest, mockEstimate, mockGroomerProfile);

        // Then
        verify(notificationService, times(1)).getFcmToken(1L);
        verify(notificationService, never()).sendNotificationWithData(any(), any(), any(), any(), any());
        verify(redisNotificationService, never()).saveNotificationToRedis(anyLong(), any(), any(), any(), any());
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
