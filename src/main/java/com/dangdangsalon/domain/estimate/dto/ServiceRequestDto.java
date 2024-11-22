package com.dangdangsalon.domain.estimate.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ServiceRequestDto {
    private Long serviceId;
    private int price;
}