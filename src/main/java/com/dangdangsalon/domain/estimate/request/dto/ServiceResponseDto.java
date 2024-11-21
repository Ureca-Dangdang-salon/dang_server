package com.dangdangsalon.domain.estimate.request.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ServiceResponseDto {
    private String description;

    public ServiceResponseDto(String description) {
        this.description = description;
    }
}