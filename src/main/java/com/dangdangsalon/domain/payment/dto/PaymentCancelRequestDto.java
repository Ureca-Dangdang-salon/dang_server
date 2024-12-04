package com.dangdangsalon.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCancelRequestDto {
    private String paymentKey;
    private String cancelReason;
}
