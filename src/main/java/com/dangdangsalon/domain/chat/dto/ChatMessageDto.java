package com.dangdangsalon.domain.chat.dto;

import com.dangdangsalon.domain.chat.entity.ChatMessageMongo;
import com.dangdangsalon.util.UUIDUtil;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {

    private Long sequence;
    private String messageId;
    private Long roomId;
    private Long senderId;
    private String senderRole;
    private String messageText;
    private String imageUrl;
    private LocalDateTime sendAt;
    private ChatEstimateInfo estimateInfo;

    public static ChatMessageDto createTextMessage(Long sequence, Long roomId, Long senderId, String senderRole, String messageText) {
        return ChatMessageDto.builder()
                .sequence(sequence)
                .messageId(UUIDUtil.generateTimeBasedUUID())
                .roomId(roomId)
                .senderId(senderId)
                .senderRole(senderRole)
                .messageText(messageText)
                .sendAt(LocalDateTime.now())
                .build();
    }

    public static ChatMessageDto createImageMessage(Long sequence, Long roomId, Long senderId, String senderRole, String imageUrl) {
        return ChatMessageDto.builder()
                .sequence(sequence)
                .messageId(UUIDUtil.generateTimeBasedUUID())
                .roomId(roomId)
                .senderId(senderId)
                .senderRole(senderRole)
                .imageUrl(imageUrl)
                .sendAt(LocalDateTime.now())
                .build();
    }

    public static ChatMessageDto mongoMessageToDto(ChatMessageMongo messages) {
        return ChatMessageDto.builder()
                .sequence(messages.getSequence())
                .messageId(messages.getId())
                .roomId(messages.getRoomId())
                .senderId(messages.getSenderId())
                .senderRole(messages.getSenderRole())
                .messageText(messages.getMessageText())
                .imageUrl(messages.getImageUrl())
                .sendAt(messages.getSendAt())
                .build();
    }

    public void updateSequence(Long sequence) {
        this.sequence = sequence;
    }
}
