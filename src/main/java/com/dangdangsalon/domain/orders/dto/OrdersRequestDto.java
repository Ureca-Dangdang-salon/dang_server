package com.dangdangsalon.domain.orders.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrdersRequestDto {
    private int amount;
    private String tossOrderId;
    private String orderName;
}
