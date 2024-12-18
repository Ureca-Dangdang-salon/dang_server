package com.dangdangsalon.domain.estimate.request.dto;
import com.dangdangsalon.domain.dogprofile.dto.DogProfileResponseDto;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestProfiles;
import com.dangdangsalon.domain.mypage.dto.res.UserProfileResponseDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class EstimateDetailResponseDto {

    private EstimateRequestUserProfileResponseDto userProfile;
    private DogProfileResponseDto dogProfileResponseDto;
    private String currentPhotoKey;
    private String styleRefPhotoKey;
    private boolean aggression;
    private boolean healthIssue;
    private String description;
    private List<ServiceResponseDto> serviceList;
    private List<FeatureResponseDto> featureList;

    @Builder
    public EstimateDetailResponseDto(EstimateRequestUserProfileResponseDto userProfile, DogProfileResponseDto dogProfileResponseDto, String currentPhotoKey, String styleRefPhotoKey,
                                     boolean aggression,
                                     boolean healthIssue,
                                     String description,
                                     List<ServiceResponseDto> serviceList,
                                     List<FeatureResponseDto> featureList) {
        this.userProfile = userProfile;
        this.dogProfileResponseDto = dogProfileResponseDto;
        this.currentPhotoKey = currentPhotoKey;
        this.styleRefPhotoKey = styleRefPhotoKey;
        this.aggression = aggression;
        this.healthIssue = healthIssue;
        this.description = description;
        this.serviceList = serviceList;
        this.featureList = featureList;
    }

    public static EstimateDetailResponseDto toDto(EstimateRequestProfiles profile,
                                                  DogProfileResponseDto dogProfileResponseDto,
                                                  List<ServiceResponseDto> serviceList,
                                                  List<FeatureResponseDto> featureList) {
        return EstimateDetailResponseDto.builder()
                .dogProfileResponseDto(dogProfileResponseDto)
                .currentPhotoKey(profile.getCurrentImageKey())
                .styleRefPhotoKey(profile.getStyleRefImageKey())
                .aggression(profile.isAggression())
                .healthIssue(profile.isHealthIssue())
                .description(profile.getDescription())
                .serviceList(serviceList)
                .featureList(featureList)
                .build();
    }
}