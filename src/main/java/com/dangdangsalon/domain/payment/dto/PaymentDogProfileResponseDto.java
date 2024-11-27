package com.dangdangsalon.domain.payment.dto;

import com.dangdangsalon.domain.estimate.request.dto.ServicePriceResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PaymentDogProfileResponseDto {

    private Long profileId;
    private String dogName;
    private List<ServicePriceResponseDto> servicePriceList;
    private int aggressionCharge;
    private int healthIssueCharge;

}