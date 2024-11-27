package com.dangdangsalon.domain.estimate.request.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ServiceResponseDto {
    private Long serviceId;
    private String description;

    public ServiceResponseDto(Long serviceId, String description) {
        this.serviceId = serviceId;
        this.description = description;
    }
}