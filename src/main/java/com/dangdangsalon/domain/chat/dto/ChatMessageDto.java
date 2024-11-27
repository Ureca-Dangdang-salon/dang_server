package com.dangdangsalon.domain.chat.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {

    private Long roomId;
    private Long senderId;
    private String senderRole;
    private String messageText;
    private String imageUrl;
    private LocalDateTime sendAt;
    private ChatEstimateInfo estimateInfo;

    public static ChatMessageDto createTextMessage(Long roomId, Long senderId, String senderRole, String messageText) {
        return ChatMessageDto.builder()
                .roomId(roomId)
                .senderId(senderId)
                .senderRole(senderRole)
                .messageText(messageText)
                .sendAt(LocalDateTime.now())
                .build();
    }

    public static ChatMessageDto createImageMessage(Long roomId, Long senderId, String senderRole, String imageUrl) {
        return ChatMessageDto.builder()
                .roomId(roomId)
                .senderId(senderId)
                .senderRole(senderRole)
                .imageUrl(imageUrl)
                .sendAt(LocalDateTime.now())
                .build();
    }
}
