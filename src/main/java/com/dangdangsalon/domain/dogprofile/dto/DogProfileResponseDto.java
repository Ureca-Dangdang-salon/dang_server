package com.dangdangsalon.domain.dogprofile.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DogProfileResponseDto {
    private Long dogProfileId;
    private String profileImage;
    private String name;

    @Builder
    public DogProfileResponseDto(Long dogProfileId, String profileImage, String name) {
        this.dogProfileId = dogProfileId;
        this.profileImage = profileImage;
        this.name = name;
    }
}
