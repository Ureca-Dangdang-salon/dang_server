package com.dangdangsalon.domain.estimate.request.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class DogRequestDto {
    private Long dogProfileId;
    private String currentImageKey;
    private String styleRefImageKey;
    private boolean aggression;
    private boolean healthIssue;
    private String description;
    private List<Long> servicesOffered;
}
