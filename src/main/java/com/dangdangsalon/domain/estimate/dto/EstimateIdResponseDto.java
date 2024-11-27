package com.dangdangsalon.domain.estimate.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EstimateIdResponseDto {
    private Long estimateId;

    @Builder
    public EstimateIdResponseDto(Long estimateId){
        this.estimateId = estimateId;
    }
}
