package com.dangdangsalon.domain.orders.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdersRequestDto {
    private int amount;
    private String tossOrderId;
    private String orderName;
}
