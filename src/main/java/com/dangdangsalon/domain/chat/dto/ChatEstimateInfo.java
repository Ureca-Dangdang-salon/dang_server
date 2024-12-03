package com.dangdangsalon.domain.chat.dto;

import com.dangdangsalon.domain.payment.dto.PaymentDogProfileResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatEstimateInfo {
    private List<ChatEstimateDogProfileDto> dogProfileList;
    private int totalAmount;
}
