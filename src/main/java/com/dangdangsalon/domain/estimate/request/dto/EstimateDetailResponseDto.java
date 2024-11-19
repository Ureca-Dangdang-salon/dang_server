package com.dangdangsalon.domain.estimate.request.dto;
import com.dangdangsalon.domain.dogprofile.dto.DogProfileResponseDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class EstimateDetailResponseDto {

    private Long dogProfileId;
    private DogProfileResponseDto dogProfileResponseDto;
    private String currentPhotoKey;
    private String styleRefPhotoKey;
    private boolean aggression;
    private boolean healthIssue;
    private String description;
    private List<ServiceResponseDto> serviceList;
    private List<FeatureResponseDto> featureList;

    @Builder
    public EstimateDetailResponseDto(Long dogProfileId, DogProfileResponseDto dogProfileResponseDto, String currentPhotoKey, String styleRefPhotoKey,
                                     boolean aggression,
                                     boolean healthIssue,
                                     String description,
                                     List<ServiceResponseDto> serviceList,
                                     List<FeatureResponseDto> featureList) {
        this.dogProfileId = dogProfileId;
        this.dogProfileResponseDto = dogProfileResponseDto;
        this.currentPhotoKey = currentPhotoKey;
        this.styleRefPhotoKey = styleRefPhotoKey;
        this.aggression = aggression;
        this.healthIssue = healthIssue;
        this.description = description;
        this.serviceList = serviceList;
        this.featureList = featureList;
    }
}