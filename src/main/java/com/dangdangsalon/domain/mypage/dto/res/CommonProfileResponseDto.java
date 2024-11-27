package com.dangdangsalon.domain.mypage.dto.res;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@Builder
public class CommonProfileResponseDto {
    private String imageKey;
    private String email;
    private String district;
    private String city;
}
