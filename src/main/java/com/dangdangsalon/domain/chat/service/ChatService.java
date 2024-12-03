package com.dangdangsalon.domain.chat.service;

import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import com.dangdangsalon.util.UUIDUtil;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageService chatMessageService;

    public ChatMessageDto createAndSaveMessage(ChatMessageDto chatMessageDto, Long roomId, Long senderId, String senderRole) {
        ChatMessageDto chatMessage = ChatMessageDto.builder()
                .messageId(UUIDUtil.generateTimeBasedUUID())
                .roomId(roomId)
                .senderId(senderId)
                .senderRole(senderRole)
                .messageText(chatMessageDto.getMessageText())
                .imageUrl(chatMessageDto.getImageUrl())
                .sendAt(LocalDateTime.now())
                .build();

        chatMessageService.saveMessageRedis(chatMessage);
        chatMessageService.updateLastReadKey(chatMessage);

        return chatMessage;
    }
}
