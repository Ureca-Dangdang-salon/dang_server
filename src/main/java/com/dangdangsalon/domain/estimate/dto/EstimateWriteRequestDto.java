package com.dangdangsalon.domain.estimate.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class EstimateWriteRequestDto {

    private Long requestId;
    private Long groomerProfileId;
    private int aggressionCharge;
    private int healthIssueCharge;
    private String description;
    private String imageKey;
    private int totalAmount;
    private LocalDateTime date;
    private List<DogPriceRequestDto> dogPriceList;

}
