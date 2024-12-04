package com.dangdangsalon.domain.chat.controller;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import com.dangdangsalon.domain.chat.service.ChatService;
import com.dangdangsalon.domain.chat.util.KafkaChatMessageProducer;
import com.dangdangsalon.util.KafkaTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

//    private final ChatService chatService;
        private final KafkaChatMessageProducer kafkaProducer;

//    @SendTo("/sub/chat/{roomId}") // 메시지 브로커가 해당 roomId를 구독한 클라이언트로 메시지 브로드 캐스트
    @MessageMapping("/chat/send/{roomId}") //클라이언트가 이 경로로 메시지를 전송한다.
    public void sendMessage(@DestinationVariable Long roomId, @Payload ChatMessageDto message,
                                      SimpMessageHeaderAccessor headerAccessor) {
        log.info("message= " + message);

        Authentication auth = (Authentication) headerAccessor.getUser();
        CustomOAuth2User user = (CustomOAuth2User) auth.getPrincipal();

        Long senderId = user.getUserId();
        String senderRole = user.getRole();

        message.updateSenderInfo(roomId, senderId, senderRole);

        kafkaProducer.sendMessage(KafkaTopic.CHAT_TOPIC.getTopic(), message);

//        return chatService.createAndSaveMessage(message, roomId, senderId, senderRole);
//        return chatService.createAndSaveMessage(message); // 테스트 용도
    }
}
