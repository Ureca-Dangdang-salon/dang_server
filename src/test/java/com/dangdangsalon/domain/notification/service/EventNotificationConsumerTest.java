package com.dangdangsalon.domain.notification.service;

import com.dangdangsalon.domain.notification.dto.EventNotificationDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventNotificationConsumerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private EventNotificationConsumer consumer;

    @Test
    @DisplayName("이벤트 알림 소비 테스트 - 성공")
    void testConsumeEventNotification_Success() {
        // Given
        EventNotificationDto notification = EventNotificationDto.builder()
                .fcmToken(List.of("token1", "token2"))
                .title("Test Title")
                .message("Test Message")
                .referenceId(1L)
                .build();

        // When
        consumer.consumeEventNotification(notification);

        // Then
        verify(notificationService, times(2)).sendNotificationWithData(anyString(), eq("Test Title"), eq("Test Message"), eq("event"), eq(1L));
    }

    @Test
    @DisplayName("이벤트 알림 소비 테스트 - FCM 토큰 없음")
    void testConsumeEventNotification_NoTokens() {
        // Given
        EventNotificationDto notification = EventNotificationDto.builder()
                .fcmToken(List.of())
                .title("Test Title")
                .message("Test Message")
                .referenceId(1L)
                .build();

        // When
        consumer.consumeEventNotification(notification);

        // Then
        verify(notificationService, never()).sendNotificationWithData(anyString(), anyString(), anyString(), anyString(), anyLong());
    }
}
