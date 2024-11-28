package com.dangdangsalon.domain.mypage.dto.res;

import com.dangdangsalon.domain.groomerprofile.entity.GroomerCanService;
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

    public static GroomerServicesResponseDto fromEntity(GroomerCanService groomerCanService) {
        return GroomerServicesResponseDto.builder()
                .description(groomerCanService.getGroomerService().getDescription())
                .isCustom(groomerCanService.getGroomerService().getIsCustom())
                .build();
    }
}
