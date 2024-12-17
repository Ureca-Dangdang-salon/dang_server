package com.dangdangsalon.domain.estimate.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstimateRequestUserProfileResponseDto {

    private String name;
    private LocalDate date;
    private String serviceType;
    private String region;
    private String imageKey;

}
