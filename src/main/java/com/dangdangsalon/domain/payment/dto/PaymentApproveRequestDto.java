package com.dangdangsalon.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentApproveRequestDto {
    private String paymentKey;
    private String orderId;
    private int amount;
    private Long couponId;
}