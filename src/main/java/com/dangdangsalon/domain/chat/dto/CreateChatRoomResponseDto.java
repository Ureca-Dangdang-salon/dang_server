package com.dangdangsalon.domain.chat.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateChatRoomResponseDto {

    private Long roomId;
    private LocalDateTime createdAt;
}
