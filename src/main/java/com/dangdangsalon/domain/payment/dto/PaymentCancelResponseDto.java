package com.dangdangsalon.domain.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentCancelResponseDto {
    private String paymentKey;
    private String orderId;
    private String status;
}