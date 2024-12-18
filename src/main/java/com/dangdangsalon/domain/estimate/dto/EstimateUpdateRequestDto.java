package com.dangdangsalon.domain.estimate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstimateUpdateRequestDto {

    private Long estimateId;
    private String description;  // 수정할 설명
    private String imageKey;     // 수정할 이미지 키
    private Integer totalAmount; // 수정할 총 금액
    private LocalDateTime date;  // 수정할 날짜
    private List<DogPriceRequestDto> dogPriceList; // 수정할 강아지별 정보
}
