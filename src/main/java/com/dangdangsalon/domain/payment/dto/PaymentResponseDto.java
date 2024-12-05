package com.dangdangsalon.domain.payment.dto;

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
public class PaymentResponseDto {

    private String groomerName;
    private String groomerImage;
    private LocalDateTime paymentDate;
    private LocalDateTime reservationDate;
    private List<PaymentDogProfileResponseDto> dogProfileList;
    private int totalAmount;
    private String status;
}
