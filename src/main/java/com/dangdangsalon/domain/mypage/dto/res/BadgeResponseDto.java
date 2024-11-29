package com.dangdangsalon.domain.mypage.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
// 미용사 뱃지 (응답)
public class BadgeResponseDto {
    private Long badgeId;
    private String name;
    private String badgeImage;
}
