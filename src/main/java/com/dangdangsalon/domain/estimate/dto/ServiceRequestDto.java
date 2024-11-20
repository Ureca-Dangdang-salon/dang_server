package com.dangdangsalon.domain.estimate.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ServiceRequestDto {
    private Long serviceId; // 서비스 ID
    private int price;      // 서비스 가격
}