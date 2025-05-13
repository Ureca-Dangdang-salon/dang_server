package com.dangdangsalon.domain.chat.util;

import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import com.dangdangsalon.domain.chat.entity.FailedChatMessage;
import com.dangdangsalon.domain.chat.repository.FailedChatMessageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaChatDlqConsumer {

    private final FailedChatMessageRepository repository;
    private final Counter dlqCounter;

    public KafkaChatDlqConsumer(MeterRegistry registry, FailedChatMessageRepository repository) {
        this.repository = repository;
        this.dlqCounter = Counter.builder("chat_message_dlq_count")
                .description("DLQ 수신 메시지 수")
                .register(registry);
    }

    @KafkaListener(topics = "chat-topic.DLT", groupId = "chat-dlq-consumer")
    public void consumeDlqMessage(ChatMessageDto message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                  @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                  @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exception)
            throws JsonProcessingException {

        log.warn("DLQ 수신: message={}, exception={}", message, exception);
        dlqCounter.increment();

        FailedChatMessage msg = FailedChatMessage.builder()
                .roomId(message.getRoomId())
                .message(new ObjectMapper().writeValueAsString(message))
                .exception(exception)
                .topic(topic)
                .partition(partition)
                .failedAt(LocalDateTime.now())
                .replayed(false)
                .build();

        repository.save(msg);
    }

    private String extractRoomId(String message) {
        try {
            JsonNode node = new ObjectMapper().readTree(message);
            return node.get("roomId").asText();
        } catch (Exception e) {
            log.warn("roomId 추출 실패: {}", message);
            return "unknown";
        }
    }
}
