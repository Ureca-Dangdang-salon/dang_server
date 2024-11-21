package com.dangdangsalon.domain.estimate.request.dto;

import com.dangdangsalon.domain.estimate.request.entity.RequestStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
public class MyEstimateRequestResponseDto {
    private List<DogNameResponseDto> dogList;
    private LocalDate date;
    private RequestStatus status;

    @Builder
    public MyEstimateRequestResponseDto(List<DogNameResponseDto> dogList, LocalDate date, RequestStatus status) {
        this.dogList = dogList;
        this.date = date;
        this.status = status;
    }
}
