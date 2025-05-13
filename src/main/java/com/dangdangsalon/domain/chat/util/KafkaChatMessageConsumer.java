package com.dangdangsalon.domain.chat.util;

import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import com.dangdangsalon.domain.chat.service.ChatMessageService;
import com.dangdangsalon.domain.chat.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.annotation.NewSpan;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaChatMessageConsumer {

    private final ChatService chatService;
    private final SimpMessageSendingOperations messageTemplate;

    @NewSpan("ConsumeChat")
    @KafkaListener(topics = "chat-topic", groupId = "chat-group", concurrency = "3")
    public void consumeMessage(ChatMessageDto message,
                               @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
//        log.info("Kafka에서 받은 메시지: {}", message);

        if ("fail".equalsIgnoreCase(message.getMessageText())) {
            throw new RuntimeException("실패 유도");
        }

        chatService.createAndSaveMessage(message);

        String destination = "/sub/chat/" + message.getRoomId();
        messageTemplate.convertAndSend(destination, message);

//        log.info("STOMP 브로드캐스트 완료: destination= {}, message= {}", destination, message);
        log.info("[roomId={}, partition={}, content={}]",
                message.getRoomId(), partition, message.getMessageText());
    }

    @KafkaListener(topics = "chat-topic.DLT", groupId = "chat-dlq-group")
    public void consumeDlq(ChatMessageDto message) {
        log.warn("DLQ에서 받은 메시지: {}", message);
    }
}
