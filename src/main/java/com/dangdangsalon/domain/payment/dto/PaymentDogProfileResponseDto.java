package com.dangdangsalon.domain.payment.dto;

import com.dangdangsalon.domain.estimate.request.dto.ServicePriceResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDogProfileResponseDto {

    private Long profileId;
    private String dogName;
    private List<ServicePriceResponseDto> servicePriceList;
    private int aggressionCharge;
    private int healthIssueCharge;

}