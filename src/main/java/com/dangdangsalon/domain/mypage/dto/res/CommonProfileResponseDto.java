package com.dangdangsalon.domain.mypage.dto.res;


import com.dangdangsalon.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CommonProfileResponseDto {
    private String imageKey;
    private String name;
    private String email;
    private String district;
    private String city;

    public static CommonProfileResponseDto createCommonProfileResponseDto(User user) {
        return CommonProfileResponseDto.builder()
                .imageKey(user.getImageKey())
                .name(user.getName())
                .email(user.getEmail())
                .city(user.getDistrict().getCity().getName())
                .district(user.getDistrict().getName())
                .build();
    }
}
