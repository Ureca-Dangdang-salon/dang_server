package com.dangdangsalon.domain.estimate.dto;

import com.dangdangsalon.domain.dogprofile.dto.DogProfileResponseDto;
import com.dangdangsalon.domain.estimate.request.dto.ServicePriceResponseDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class EstimateDogResponseDto {

    private DogProfileResponseDto dogProfileResponseDto;
    private String description;
    private int dogPrice;
    private List<ServicePriceResponseDto> serviceList;
    private boolean aggression;
    private boolean healthIssue;

    @Builder
    public EstimateDogResponseDto(DogProfileResponseDto dogProfileResponseDto, String description, List<ServicePriceResponseDto> serviceList, boolean isAggression, boolean isHealthIssue, int dogPrice) {
        this.dogProfileResponseDto = dogProfileResponseDto;
        this.description = description;
        this.serviceList = serviceList;
        this.aggression = isAggression;
        this.healthIssue = isHealthIssue;
        this.dogPrice = dogPrice;
    }
}
