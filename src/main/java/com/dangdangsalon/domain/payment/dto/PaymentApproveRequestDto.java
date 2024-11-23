package com.dangdangsalon.domain.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentApproveRequestDto {
    private String paymentKey;
    private String orderId;
    private int amount;
}