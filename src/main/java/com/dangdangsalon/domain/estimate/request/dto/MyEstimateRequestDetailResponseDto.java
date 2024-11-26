package com.dangdangsalon.domain.estimate.request.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class MyEstimateRequestDetailResponseDto {
    private String imageKey;
    private String dogName;
    private boolean aggression;
    private boolean healthIssue;
    private String description;
    private List<ServiceResponseDto> serviceList;

    @Builder
    public MyEstimateRequestDetailResponseDto(String imageKey, String dogName, boolean aggression, boolean healthIssue, String description, List<ServiceResponseDto> serviceList) {
        this.imageKey = imageKey;
        this.dogName = dogName;
        this.aggression = aggression;
        this.healthIssue = healthIssue;
        this.description = description;
        this.serviceList = serviceList;
    }
}
