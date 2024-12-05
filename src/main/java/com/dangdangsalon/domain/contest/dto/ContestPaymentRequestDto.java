package com.dangdangsalon.domain.contest.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContestPaymentRequestDto {

    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
