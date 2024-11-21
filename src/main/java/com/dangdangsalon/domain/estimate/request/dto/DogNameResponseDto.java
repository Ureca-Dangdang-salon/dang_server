package com.dangdangsalon.domain.estimate.request.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DogNameResponseDto {
    private String dogName;

    @Builder
    public DogNameResponseDto(String dogName) {
        this.dogName = dogName;
    }
}
