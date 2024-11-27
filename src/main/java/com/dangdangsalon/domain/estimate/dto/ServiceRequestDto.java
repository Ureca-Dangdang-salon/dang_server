package com.dangdangsalon.domain.estimate.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ServiceRequestDto {
    private Long serviceId;
    private int price;
}