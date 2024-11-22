package com.dangdangsalon.domain.estimate.request.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ServicePriceResponseDto {
    private Long serviceId;
    private String description;
    private int price;

    public ServicePriceResponseDto(Long serviceId, String description, int price) {
        this.price = price;
        this.serviceId = serviceId;
        this.description = description;
    }
}