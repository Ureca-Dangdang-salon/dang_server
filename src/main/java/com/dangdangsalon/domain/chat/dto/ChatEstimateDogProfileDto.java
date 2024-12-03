package com.dangdangsalon.domain.chat.dto;

import com.dangdangsalon.domain.estimate.request.dto.ServicePriceResponseDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatEstimateDogProfileDto {
    private String dogName;
    private List<ServicePriceResponseDto> servicePriceList;
    private int aggressionCharge;
    private int healthIssueCharge;
}
