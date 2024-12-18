package com.dangdangsalon.domain.notification.service;

import com.dangdangsalon.domain.notification.dto.EventNotificationDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventNotificationProducerTest {

    @Mock
    private KafkaTemplate<String, EventNotificationDto> kafkaTemplate;

    @InjectMocks
    private EventNotificationProducer producer;

    @Test
    @DisplayName("이벤트 알림 전송 테스트")
    void testSendEventNotification() {
        // Given
        EventNotificationDto notification = EventNotificationDto.builder()
                .fcmToken(List.of("token1", "token2"))
                .title("Test Title")
                .message("Test Message")
                .referenceId(1L)
                .build();

        // When
        producer.sendEventNotification(notification);

        // Then
        verify(kafkaTemplate, times(1)).send(eq("event-alerts"), eq(notification));
    }
}
