package com.dangdangsalon.domain.mypage.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class GroomerMainResponseDto {
    private List<GroomerRecommendResponseDto> districtTopGroomers;
    private List<GroomerRecommendResponseDto> nationalTopGroomers;
}
