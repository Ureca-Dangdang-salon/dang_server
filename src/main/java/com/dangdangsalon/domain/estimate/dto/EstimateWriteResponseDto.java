package com.dangdangsalon.domain.estimate.dto;

import com.dangdangsalon.domain.dogprofile.dto.DogProfileResponseDto;
import com.dangdangsalon.domain.estimate.request.dto.ServiceResponseDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class EstimateWriteResponseDto {

    private DogProfileResponseDto dogProfileResponseDto;
    private String description;
    private List<ServiceResponseDto> serviceList;
    private boolean aggression;
    private boolean healthIssue;

    @Builder
    public EstimateWriteResponseDto(DogProfileResponseDto dogProfileResponseDto,String description, List<ServiceResponseDto> serviceList, boolean isAggression, boolean isHealthIssue) {
        this.dogProfileResponseDto = dogProfileResponseDto;
        this.description = description;
        this.serviceList = serviceList;
        this.aggression = isAggression;
        this.healthIssue = isHealthIssue;
    }
}
