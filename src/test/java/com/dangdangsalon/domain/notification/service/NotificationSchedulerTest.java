package com.dangdangsalon.domain.notification.service;

import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.repository.EstimateRepository;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.notification.dto.NotificationDto;
import com.dangdangsalon.domain.notification.dto.ReviewNotificationDto;
import com.dangdangsalon.domain.user.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("NotificationSchedulerTest")
class NotificationSchedulerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationEmailService notificationEmailService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EstimateRepository estimateRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ListOperations<String, String> listOperations;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private RedisNotificationService redisNotificationService;

    @InjectMocks
    private NotificationScheduler notificationScheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // RedisTemplate operations Mock 설정
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("내일 예약 알림 전송 성공")
    void sendReservationReminder_Success() {
        // Given
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        Estimate mockEstimate = mock(Estimate.class);

        EstimateRequest mockRequest = mock(EstimateRequest.class);
        User mockUser = mock(User.class);

        // Mock 데이터 설정
        when(mockEstimate.getDate()).thenReturn(tomorrow);
        when(mockEstimate.getId()).thenReturn(1L);
        when(mockEstimate.getEstimateRequest()).thenReturn(mockRequest);
        when(mockRequest.getUser()).thenReturn(mockUser);
        when(mockUser.getId()).thenReturn(1L);
        when(mockUser.getEmail()).thenReturn("test1234@naver.com");

        when(estimateRepository.findReservationsForTomorrow(any(), any())).thenReturn(List.of(mockEstimate));
        when(notificationService.getFcmToken(1L)).thenReturn(Optional.of("dummyFcmToken"));

        // When
        notificationScheduler.sendReservationReminder();

        // Then
        verify(notificationService, times(1)).sendNotificationWithData(
                eq("dummyFcmToken"),
                eq("예약일 알림"),
                anyString(),
                eq("RESERVATION_REMINDER"),
                eq(1L)
        );
        verify(notificationEmailService, times(1)).sendEmail(eq("test1234@naver.com"), eq("예약일 알림"), anyString());
        verify(redisNotificationService, times(1)).saveNotificationToRedis(eq(1L), eq("예약일 알림"), anyString(), eq("RESERVATION_REMINDER"), eq(1L));
    }

    @Test
    @DisplayName("오래된 알림 제거 성공")
    void removeOldNotifications_Success() throws JsonProcessingException {
        // Given
        String key = "notifications:1";

        // 만료된 알림 데이터
        NotificationDto oldNotification = NotificationDto.builder()
                .createdAt(LocalDateTime.now().minusDays(15)) // 오래된 알림
                .id("oldNotification123")
                .title("Old Notification")
                .body("This is an old notification")
                .type("REMINDER")
                .referenceId(123L)
                .isRead(false)
                .build();

        String oldNotificationJson = objectMapper.writeValueAsString(oldNotification);

        // Mock 설정
        when(redisTemplate.keys("notifications:*")).thenReturn(Set.of(key));
        when(listOperations.range(key, 0, -1)).thenReturn(Collections.singletonList(oldNotificationJson));
        when(objectMapper.readValue(eq(oldNotificationJson), eq(NotificationDto.class))).thenReturn(oldNotification);

        // When
        notificationScheduler.removeOldNotifications();

        // Then
        verify(listOperations, times(1)).remove(eq(key), eq(1L), eq(oldNotificationJson));
    }


    @Test
    @DisplayName("리뷰 작성 요청 알림 성공")
    void sendReviewReminders_Success() throws JsonProcessingException {
        // Given
        String key = "review_notification:1";
        String jsonData = "{\"userId\":1,\"estimateId\":1,\"scheduledTime\":\"" + LocalDateTime.now().minusMinutes(1).toString() + "\"}";
        ReviewNotificationDto mockReminder = new ReviewNotificationDto(1L, 1L, LocalDateTime.now().minusMinutes(1).toString());
        Estimate mockEstimate = mock(Estimate.class);

        GroomerProfile mockGroomerProfile = mock(GroomerProfile.class);
        when(mockGroomerProfile.getName()).thenReturn("테스트 미용사");

        when(mockEstimate.getId()).thenReturn(1L);
        when(mockEstimate.getGroomerProfile()).thenReturn(mockGroomerProfile);

        when(redisTemplate.keys("review_notification:*")).thenReturn(Set.of(key));
        when(valueOperations.get(key)).thenReturn(jsonData);
        when(objectMapper.readValue(eq(jsonData), eq(ReviewNotificationDto.class))).thenReturn(mockReminder);
        when(estimateRepository.findWithEstimateById(1L)).thenReturn(Optional.of(mockEstimate));
        when(notificationService.getFcmToken(1L)).thenReturn(Optional.of("dummyFcmToken"));

        // When
        notificationScheduler.sendReviewReminders();

        // Then
        verify(notificationService, times(1)).sendNotificationWithData(
                eq("dummyFcmToken"),
                eq("리뷰 작성 요청"),
                eq("테스트 미용사님에 대한 리뷰를 작성해주세요!"),
                eq("REVIEW_REQUEST"),
                eq(1L)
        );
        verify(redisTemplate, times(1)).delete(key);
    }
}
