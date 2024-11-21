package com.dangdangsalon.domain.auth.dto;

import com.dangdangsalon.domain.region.entity.District;
import com.dangdangsalon.domain.user.entity.Role;
import com.dangdangsalon.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDto {

    private Long userId;
    private String role;
    private String name;
    private String username;
    private String email;
    private String image;
    private District district;

    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().toString())
                .image(user.getImageKey())
                .district(user.getDistrict())
                .build();
    }
}
