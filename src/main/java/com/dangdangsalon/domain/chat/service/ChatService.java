package com.dangdangsalon.domain.chat.service;

import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import com.dangdangsalon.util.UUIDUtil;
import io.micrometer.tracing.annotation.NewSpan;
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
                .estimateInfo(chatMessageDto.getEstimateInfo())
                .sendAt(LocalDateTime.now())
                .build();

        chatMessageService.saveMessageRedis(chatMessage);
        chatMessageService.updateLastReadKey(chatMessage);

        return chatMessage;
    }

    @NewSpan("CreateAndSaveMessage")
    public ChatMessageDto createAndSaveMessage(ChatMessageDto chatMessageDto) {
        ChatMessageDto chatMessage = ChatMessageDto.builder()
                .messageId(UUIDUtil.generateTimeBasedUUID())
                .roomId(chatMessageDto.getRoomId())
                .senderId(chatMessageDto.getSenderId())
                .senderRole(chatMessageDto.getSenderRole())
                .messageText(chatMessageDto.getMessageText())
                .imageUrl(chatMessageDto.getImageUrl())
                .sendAt(LocalDateTime.now())
                .build();

        chatMessageService.saveMessageRedis(chatMessage);
        chatMessageService.updateLastReadKey(chatMessage);

        return chatMessage;
    } //테스트 용도
}
