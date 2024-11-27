package com.dangdangsalon.domain.payment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class PaymentApproveResponseDto {

    private String paymentKey;
    private int totalAmount;
    private String status;
    private OffsetDateTime approvedAt;
    private String method;
}
