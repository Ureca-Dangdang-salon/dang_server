package com.dangdangsalon.domain.estimate.request.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class EstimateResponseDto {
    private String name;
    private LocalDate date;
    private String serviceType;
    private String region;
    private String imageKey;
    private String estimateRequestStatus;
    private String groomerEstimateRequestStatus;

    @Builder
    public EstimateResponseDto(String name, LocalDate date, String serviceType, String region, String status, String imageKey, String estimateRequestStatus, String groomerEstimateRequestStatus) {
        this.name = name;
        this.date = date;
        this.serviceType = serviceType;
        this.region = region;
        this.imageKey = imageKey;
        this.estimateRequestStatus = estimateRequestStatus;
        this.groomerEstimateRequestStatus = groomerEstimateRequestStatus;
    }
}
