package com.dangdangsalon.domain.estimate.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class EstimateResponseDto {

    private String comment;
    private int totalAmount;
    private LocalDateTime date;
    private List<EstimateDogResponseDto> estimateList;

    @Builder
    public EstimateResponseDto(String comment, int totalAmount, LocalDateTime date, List<EstimateDogResponseDto> estimateList) {
        this.comment = comment;
        this.totalAmount = totalAmount;
        this.date = date;
        this.estimateList = estimateList;
    }
}
