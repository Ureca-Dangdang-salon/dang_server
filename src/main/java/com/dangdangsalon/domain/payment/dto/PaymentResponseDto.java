package com.dangdangsalon.domain.payment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PaymentResponseDto {

    private String groomerName;
    private String groomerImage;
    private LocalDateTime paymentDate;
    private List<PaymentDogProfileResponseDto> dogProfileList;
    private int totalAmount;
    private String status;
}
