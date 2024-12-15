package com.dangdangsalon.domain.coupon.service;

import com.dangdangsalon.config.RedisPublisher;
import com.dangdangsalon.domain.coupon.dto.QueueStatusDto;
import com.dangdangsalon.domain.coupon.listener.RedisMessageSubscriber;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.Message;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisMessageSubscriberTest {

    @InjectMocks
    private RedisMessageSubscriber redisMessageSubscriber;

    @Mock
    private RedisPublisher redisPublisher;

    @Mock
    private ObjectMapper objectMapper;

    private static final String QUEUE_STATUS_CHANNEL = "queue_status";

    private String validJsonMessage;

    @BeforeEach
    void setUp() throws Exception {
        validJsonMessage = "{\"queueLength\": 10}";
        when(objectMapper.readValue(validJsonMessage, String.class)).thenReturn(validJsonMessage);
        when(objectMapper.readTree(validJsonMessage)).thenReturn(
                new ObjectMapper().readTree(validJsonMessage));
        when(objectMapper.convertValue(any(), eq(QueueStatusDto.class))).thenReturn(
                QueueStatusDto.builder()
                        .queueLength(10)
                        .build()
        );
    }

    @Test
    @DisplayName("queue_status 채널 메시지 처리 테스트")
    void processQueueStatusMessage_success(){
        // Given
        Message mockMessage = mock(Message.class);
        when(mockMessage.getBody()).thenReturn(validJsonMessage.getBytes(StandardCharsets.UTF_8));
        when(mockMessage.getChannel()).thenReturn(QUEUE_STATUS_CHANNEL.getBytes(StandardCharsets.UTF_8));

        // When
        redisMessageSubscriber.onMessage(mockMessage, null);

        // Then
        ArgumentCaptor<QueueStatusDto> captor = ArgumentCaptor.forClass(QueueStatusDto.class);
        verify(redisPublisher, times(1)).broadcast(captor.capture(), eq("queueStatus"));
        assertThat(captor.getValue().getQueueLength()).isEqualTo(10);
    }

    @Test
    @DisplayName("잘못된 메시지 처리 시 예외 처리 확인")
    void processQueueStatusMessage_invalidMessage(){
        // Given
        Message mockMessage = mock(Message.class);
        String invalidJson = "invalid-json";
        when(mockMessage.getBody()).thenReturn(invalidJson.getBytes(StandardCharsets.UTF_8));
        when(mockMessage.getChannel()).thenReturn(QUEUE_STATUS_CHANNEL.getBytes(StandardCharsets.UTF_8));

        // When
        redisMessageSubscriber.onMessage(mockMessage, null);

        // Then
        verify(redisPublisher, never()).broadcast(any(), anyString());
    }
}