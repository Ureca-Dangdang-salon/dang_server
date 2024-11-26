package com.dangdangsalon.domain.estimate.dto;

import com.dangdangsalon.domain.estimate.entity.EstimateStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class MyEstimateDetailResponseDto {

    private EstimateStatus status;
    private String description;
    private String imageKey;
    private int totalAmount;
    private LocalDateTime date;
    private String startChat;

    @Builder
    public MyEstimateDetailResponseDto(EstimateStatus status, String description, String imageKey, int totalAmount, LocalDateTime date, String startChat) {
        this.status = status;
        this.description = description;
        this.imageKey = imageKey;
        this.totalAmount = totalAmount;
        this.date = date;
        this.startChat = startChat;
    }
}
