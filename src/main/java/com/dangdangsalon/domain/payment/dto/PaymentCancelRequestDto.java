package com.dangdangsalon.domain.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentCancelRequestDto {
    private String paymentKey;
    private String cancelReason;
}
