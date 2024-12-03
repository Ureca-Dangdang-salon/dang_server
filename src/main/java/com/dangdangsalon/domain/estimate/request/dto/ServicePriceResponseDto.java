package com.dangdangsalon.domain.estimate.request.dto;

import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestService;
import com.dangdangsalon.domain.groomerservice.entity.GroomerService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ServicePriceResponseDto {
    private Long serviceId;
    private String description;
    private int price;

    @Builder
    public ServicePriceResponseDto(Long serviceId, String description, int price) {
        this.price = price;
        this.serviceId = serviceId;
        this.description = description;
    }

    public static ServicePriceResponseDto create(EstimateRequestService service) {
        return ServicePriceResponseDto.builder()
                .serviceId(service.getGroomerService().getId())
                .description(service.getGroomerService().getDescription())
                .price(service.getPrice())
                .build();
    }
}