package com.dangdangsalon.domain.chat.dto;

import com.dangdangsalon.domain.chat.entity.ChatRoom;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatGroomerProfileDto {

    private Long groomerProfileId;
    private String address;
    private String serviceName;
    private String profileImageUrl;

    public static ChatGroomerProfileDto create(ChatRoom chatRoom) {
        GroomerProfile groomerProfile = chatRoom.getGroomerProfile();

        return ChatGroomerProfileDto.builder()
                .groomerProfileId(groomerProfile.getId())
                .address(groomerProfile.getDetails().getAddress())
                .serviceName(groomerProfile.getName())
                .profileImageUrl(groomerProfile.getImageKey())
                .build();
    }
}
