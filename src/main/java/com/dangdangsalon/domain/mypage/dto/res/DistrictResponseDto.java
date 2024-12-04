package com.dangdangsalon.domain.mypage.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
// 미용사 서비스 지역 (응답)
public class DistrictResponseDto {
    private String district;
    private String city;
}
