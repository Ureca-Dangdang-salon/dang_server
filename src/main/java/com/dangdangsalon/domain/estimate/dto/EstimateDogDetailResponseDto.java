package com.dangdangsalon.domain.estimate.dto;

import com.dangdangsalon.domain.dogprofile.entity.DogProfile;
import com.dangdangsalon.domain.dogprofile.entity.Gender;
import com.dangdangsalon.domain.dogprofile.entity.Neutering;
import com.dangdangsalon.domain.estimate.request.dto.FeatureResponseDto;
import com.dangdangsalon.domain.estimate.request.dto.ServicePriceResponseDto;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestProfiles;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
public class EstimateDogDetailResponseDto {

    private String dogName;
    private int year;
    private int month;
    private int dogWeight;
    private Gender gender;
    private Neutering neutering;
    private String imageKey;
    private String currentImageKey;
    private String styleRefImageKey;
    private String species;
    private int aggressionCharge;
    private int healthIssueCharge;
    private List<ServicePriceResponseDto> serviceList;
    private boolean aggression;
    private boolean healthIssue;
    private String description;
    private List<FeatureResponseDto> featureList;

    public static EstimateDogDetailResponseDto toDto(
            DogProfile dogProfile,
            EstimateRequestProfiles profile,
            List<ServicePriceResponseDto> serviceList,
            List<FeatureResponseDto> featureList
    ) {
        return EstimateDogDetailResponseDto.builder()
                .dogName(dogProfile.getName())
                .year(dogProfile.getAge().getYear())
                .month(dogProfile.getAge().getMonth())
                .dogWeight(dogProfile.getWeight())
                .gender(dogProfile.getGender())
                .neutering(dogProfile.getNeutering())
                .imageKey(dogProfile.getImageKey())
                .currentImageKey(profile.getCurrentImageKey())
                .styleRefImageKey(profile.getStyleRefImageKey())
                .species(dogProfile.getSpecies())
                .serviceList(serviceList)
                .aggression(profile.isAggression())
                .healthIssue(profile.isHealthIssue())
                .aggressionCharge(profile.getAggressionCharge())
                .healthIssueCharge(profile.getHealthIssueCharge())
                .description(profile.getDescription())
                .featureList(featureList)
                .build();
    }
}
