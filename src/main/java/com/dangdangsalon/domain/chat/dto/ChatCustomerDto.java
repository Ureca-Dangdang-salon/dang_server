package com.dangdangsalon.domain.chat.dto;

import com.dangdangsalon.domain.chat.entity.ChatRoom;
import com.dangdangsalon.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatCustomerDto {

    private Long customerId;
    private String customerName;
    private String profileImageUrl;

    public static ChatCustomerDto create(ChatRoom chatRoom) {
        User customer = chatRoom.getCustomer();

        return ChatCustomerDto.builder()
                .customerId(customer.getId())
                .customerName(customer.getName())
                .profileImageUrl(customer.getImageKey())
                .build();
    }
}
