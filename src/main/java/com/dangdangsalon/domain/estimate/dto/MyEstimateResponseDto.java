package com.dangdangsalon.domain.estimate.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MyEstimateResponseDto {
    private int totalAmount;

    @Builder
    public MyEstimateResponseDto(int totalAmount) {
        this.totalAmount = totalAmount;
    }
}
