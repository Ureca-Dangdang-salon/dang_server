package com.dangdangsalon.domain.estimate.request.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class EstimateRequestDto {
    private Long groomerProfileId;
    private Long districtId;
    private LocalDateTime date;
    private String serviceType;
    private List<DogEstimateRequestDto> dogEstimateRequestList;

    public EstimateRequestDto(Long groomerProfileId, Long districtId, LocalDateTime date, String serviceType, List<DogEstimateRequestDto> dogEstimateRequestList) {
        this.groomerProfileId = groomerProfileId;
        this.districtId = districtId;
        this.date = date;
        this.serviceType = serviceType;
        this.dogEstimateRequestList = dogEstimateRequestList;
    }
}