package com.dangdangsalon.domain.chat.dto;

import com.dangdangsalon.domain.chat.entity.ChatRoom;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDetailDto {

    private Long roomId;
    private Long estimateId;
    private Long estimateRequestId;
    private ChatGroomerProfileDto groomerProfile;
    private ChatCustomerDto customer;
    private List<ChatMessageDto> recentMessages;

    public static ChatRoomDetailDto create(ChatRoom chatRoom, List<ChatMessageDto> recentMessages) {
        return ChatRoomDetailDto.builder()
                .roomId(chatRoom.getId())
                .estimateId(chatRoom.getEstimate().getId())
                .estimateRequestId(chatRoom.getEstimate().getEstimateRequest().getId())
                .groomerProfile(ChatGroomerProfileDto.create(chatRoom))
                .customer(ChatCustomerDto.create(chatRoom))
                .recentMessages(recentMessages)
                .build();
    }
}
