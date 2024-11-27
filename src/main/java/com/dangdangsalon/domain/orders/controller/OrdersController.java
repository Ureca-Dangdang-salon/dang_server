package com.dangdangsalon.domain.orders.controller;

import com.dangdangsalon.domain.orders.dto.OrdersRequestDto;
import com.dangdangsalon.domain.orders.dto.OrdersResponseDto;
import com.dangdangsalon.domain.orders.service.OrdersService;
import com.dangdangsalon.util.ApiUtil;
import com.dangdangsalon.util.ApiUtil.ApiSuccess;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrdersController {

    private final OrdersService ordersService;

    // 결제 요청 응답(주문 등록)
    @PostMapping("{estimateId}")
    public ApiSuccess<?> insertOrders(@RequestBody OrdersRequestDto ordersRequestDto, @PathVariable Long estimateId) {
        OrdersResponseDto ordersResponseDto = ordersService.insertOrders(estimateId, ordersRequestDto);
        return ApiUtil.success(ordersResponseDto);
    }
}
