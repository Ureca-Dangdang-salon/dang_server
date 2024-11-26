package com.dangdangsalon.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatMessageDto {

    private Long roomId;
    private Long senderId;
    private String senderRole;
    private String messageText;
    private String imageUrl;
}
