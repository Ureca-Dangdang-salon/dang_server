package com.dangdangsalon.domain.mypage.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
// 제공 서비스 (응답)
public class GroomerServicesResponseDto {
    private String description;
    private Boolean isCustom;

}
