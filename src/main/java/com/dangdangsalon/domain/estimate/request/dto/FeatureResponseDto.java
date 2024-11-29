package com.dangdangsalon.domain.estimate.request.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
public class FeatureResponseDto {
    private String description;

    public FeatureResponseDto(String description) {
        this.description = description;
    }
}