package com.dangdangsalon.domain.chat.util;

import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaChatMessageProducer {

    private final KafkaTemplate<String, ChatMessageDto> kafkaTemplate;

    @NewSpan("sendMessageKafkaProducer")
    public void sendMessage(String topic, ChatMessageDto message) {
        String key = message.getRoomId().toString();

        kafkaTemplate.send(topic, key, message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("topic {}에 메시지 전달 실패: {}", topic, ex.getMessage(), ex);
                    } else {
                        log.info("topic {}에 메시지 전달 성공: {}", topic, message);
                    }
                });
    }
}
