package com.dangdangsalon.domain.orders.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OrdersResponseDto {
    private int amount;
    private String tossOrderId;
    private LocalDateTime requestedAt;
    private String orderName;
}
