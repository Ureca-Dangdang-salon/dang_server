package com.dangdangsalon.domain.dogprofile.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DogProfileResponseDto {
    private String profileImage;
    private String name;

    public DogProfileResponseDto(String profileImage, String name) {
        this.profileImage = profileImage;
        this.name = name;
    }
}
