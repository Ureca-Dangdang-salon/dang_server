package com.dangdangsalon.domain.estimate.request.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class EstimateRequestDto {
    private String city;
    private String district;
    private LocalDateTime date;
    private String serviceType;
    private List<DogEstimateRequestDto> dogEstimateRequestList;

    public EstimateRequestDto(String city, String district, LocalDateTime date, String serviceType, List<DogEstimateRequestDto> dogEstimateRequestList) {
        this.city = city;
        this.district = district;
        this.date = date;
        this.serviceType = serviceType;
        this.dogEstimateRequestList = dogEstimateRequestList;
    }
}