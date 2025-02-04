package com.dangdangsalon.domain.chat.dto;

import com.dangdangsalon.domain.estimate.entity.EstimateStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomListDto {

    private Long roomId;
    private Long estimateId;
    private ChatGroomerProfileDto groomerProfile;
    private ChatCustomerDto customer;
    private String lastMessage;
    private int unreadCount;
    private int totalAmount;
    private EstimateStatus estimateStatus;
}
