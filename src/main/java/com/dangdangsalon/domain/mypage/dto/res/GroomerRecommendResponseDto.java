package com.dangdangsalon.domain.mypage.dto.res;

import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class GroomerRecommendResponseDto {
    private Long profileId;
    private String name;
    private String imageKey;
    private String city;
    private String district;

    public static GroomerRecommendResponseDto create(GroomerProfile groomerProfile) {

        return GroomerRecommendResponseDto.builder()
                .profileId(groomerProfile.getId())
                .name(groomerProfile.getUser().getName())
                .imageKey(groomerProfile.getUser().getImageKey())
                .city(groomerProfile.getUser().getDistrict().getCity().getName())
                .district(groomerProfile.getUser().getDistrict().getName())
                .build();
    }
}
