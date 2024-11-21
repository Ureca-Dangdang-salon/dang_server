package com.dangdangsalon.domain.estimate.request.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class DogEstimateRequestDto {
    private Long dogProfileId;
    private String currentImageKey;
    private String styleRefImageKey;
    private boolean aggression;
    private boolean healthIssue;
    private String description;
    private List<Long> servicesOffered;

    public DogEstimateRequestDto(Long dogProfileId, String currentImageKey, String styleRefImageKey,
                                 boolean aggression, boolean healthIssue, String description, List<Long> servicesOffered) {
        this.dogProfileId = dogProfileId;
        this.currentImageKey = currentImageKey;
        this.styleRefImageKey = styleRefImageKey;
        this.aggression = aggression;
        this.healthIssue = healthIssue;
        this.description = description;
        this.servicesOffered = servicesOffered;
    }

}
