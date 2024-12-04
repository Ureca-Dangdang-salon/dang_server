package com.dangdangsalon.domain.chat.util;

import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import com.dangdangsalon.domain.chat.service.ChatMessageService;
import com.dangdangsalon.domain.chat.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaChatMessageConsumer {

    private final ChatService chatService;
    private final SimpMessageSendingOperations messageTemplate;

    @KafkaListener(topics = "chat-topic", groupId = "chat-group")
    public void consumeMessage(ChatMessageDto message) {
        log.info("Kafka에서 받은 메시지: {}", message);

        chatService.createAndSaveMessage(message);

        String destination = "/sub/chat/" + message.getRoomId();
        messageTemplate.convertAndSend(destination, message);

        log.info("STOMP 브로드캐스트 완료: destination= {}, message= {}", destination, message);
    }
}
