package com.dangdangsalon.domain.estimate.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequestDto {
    private Long serviceId;
    private int price;
}