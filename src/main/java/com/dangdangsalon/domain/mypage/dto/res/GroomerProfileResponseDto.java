package com.dangdangsalon.domain.mypage.dto.res;

import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Getter
@AllArgsConstructor
@Builder
// 미용사 페이지 조회 (응답)
public class GroomerProfileResponseDto {
    private Role role;
    private String name;
    private String email;
    private String profileImage;
    private String city;
    private String district;
    private GroomerProfileDetailsResponseDto groomerProfile;

    public static GroomerProfileResponseDto createGroomerProfileResponseDto(
            GroomerProfile groomerProfile,
            GroomerProfileDetailsResponseDto groomerProfileDetailsResponseDto
    ) {

        return GroomerProfileResponseDto.builder()
                .role(groomerProfile.getUser().getRole())
                .name(groomerProfile.getUser().getName())
                .email(groomerProfile.getUser().getEmail())
                .profileImage(groomerProfile.getUser().getImageKey())
                .city(groomerProfile.getUser().getDistrict().getCity().getName())
                .district(groomerProfile.getUser().getDistrict().getName())
                .groomerProfile(groomerProfileDetailsResponseDto)
                .build();
    }
}