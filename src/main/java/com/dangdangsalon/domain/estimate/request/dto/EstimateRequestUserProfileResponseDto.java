package com.dangdangsalon.domain.estimate.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstimateRequestUserProfileResponseDto {

    private String name;
    private LocalDateTime date;
    private String serviceType;
    private String region;
    private String imageKey;
}
