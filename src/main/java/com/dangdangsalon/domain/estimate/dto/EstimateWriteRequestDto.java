package com.dangdangsalon.domain.estimate.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class EstimateWriteRequestDto {

    private Long requestId;
    private Long groomerProfileId;
    private int aggressionCharge;
    private int healthIssueCharge;
    private String description;
    private String imageKey;
    private int totalAmount;
    private LocalDateTime date;
    private List<ServiceRequestDto> serviceList;
    private List<DogPriceRequestDto> dogPriceList;

}