package com.dangdangsalon.domain.estimate.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class DogPriceRequestDto {
    private Long dogProfileId;
    private int aggressionCharge;
    private int healthIssueCharge;
    private List<ServiceRequestDto> serviceList;
}
