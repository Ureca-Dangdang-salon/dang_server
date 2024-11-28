package com.dangdangsalon.domain.mypage.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Getter
@AllArgsConstructor
@Builder
// 미용사 페이지 조회 (응답)
public class GroomerProfileResponseDto {
    private String role;
    private String name;
    private String email;
    private String profileImage;
    private String city;
    private String district;
    private GroomerProfileDetailsResponseDto groomerProfile;
}