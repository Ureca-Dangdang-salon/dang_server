package com.dangdangsalon.domain.contest.dto;

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
public class ContestPaymentDto {

    private Long groomerProfileId;
    private String groomerName;
    private String groomerImage;
    private LocalDateTime paymentDate;
    private LocalDateTime reservationDate;
    private List<String> serviceList;
    private int totalAmount;
}
