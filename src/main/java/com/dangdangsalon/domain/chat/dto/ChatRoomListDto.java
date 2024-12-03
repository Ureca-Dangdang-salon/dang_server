package com.dangdangsalon.domain.chat.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomListDto {

    private Long roomId;
    private ChatGroomerProfileDto groomerProfile;
    private ChatCustomerDto customer;
    private String lastMessage;
    private int unreadCount;
    private int totalAmount;
}
