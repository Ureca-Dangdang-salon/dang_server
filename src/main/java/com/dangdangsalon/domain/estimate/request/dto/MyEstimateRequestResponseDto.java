package com.dangdangsalon.domain.estimate.request.dto;

import com.dangdangsalon.domain.estimate.entity.EstimateStatus;
import com.dangdangsalon.domain.estimate.request.entity.RequestStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class MyEstimateRequestResponseDto {
    private Long requestId;
    private List<DogNameResponseDto> dogList;
    private LocalDateTime date;
    private RequestStatus status;
    private EstimateStatus estimateStatus;

    @Builder
    public MyEstimateRequestResponseDto(Long requestId,List<DogNameResponseDto> dogList, LocalDateTime date, RequestStatus status, EstimateStatus estimateStatus) {
        this.requestId = requestId;
        this.dogList = dogList;
        this.date = date;
        this.status = status;
        this.estimateStatus = estimateStatus;
    }
}
