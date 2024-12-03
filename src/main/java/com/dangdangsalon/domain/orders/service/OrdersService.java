package com.dangdangsalon.domain.orders.service;

import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.entity.EstimateStatus;
import com.dangdangsalon.domain.estimate.repository.EstimateRepository;
import com.dangdangsalon.domain.orders.dto.OrdersRequestDto;
import com.dangdangsalon.domain.orders.dto.OrdersResponseDto;
import com.dangdangsalon.domain.orders.entity.OrderStatus;
import com.dangdangsalon.domain.orders.entity.Orders;
import com.dangdangsalon.domain.orders.repository.OrdersRepository;
import com.dangdangsalon.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrdersService {

    private final OrdersRepository ordersRepository;
    private final EstimateRepository estimateRepository;

    @Transactional
    public OrdersResponseDto insertOrders(Long estimateId, OrdersRequestDto ordersRequestDto) {

        Estimate estimate = estimateRepository.findById(estimateId)
                .orElseThrow(() -> new IllegalArgumentException("견적서를 찾을 수 없습니다 : " + estimateId));

        // 결제하기 버튼을 누르기만 하고 다시 뒤로가기를 했을 경우 주문은 이미 등록되어 있을거고 다시 누르면 주문테이블 조회(1대1관계)
        Optional<Orders> existingOrder = ordersRepository.findByEstimateId(estimateId);
        if (existingOrder.isPresent()) {
            Orders order = existingOrder.get();
            return OrdersResponseDto.builder()
                    .orderName(order.getOrderName())
                    .amount(order.getAmountValue())
                    .tossOrderId(order.getTossOrderId())
                    .requestedAt(order.getCreatedAt())
                    .build();
        }

        // 없으면 생성 후 응답 반환
        User user = estimate.getEstimateRequest().getUser();

        Orders order = Orders.builder()
                .orderName(ordersRequestDto.getOrderName())
                .amountValue(ordersRequestDto.getAmount())
                .status(OrderStatus.PENDING)
                .estimate(estimate)
                .user(user)
                .tossOrderId(ordersRequestDto.getTossOrderId())
                .build();
        ordersRepository.save(order);

        return OrdersResponseDto.builder()
                .orderName(order.getOrderName())
                .amount(order.getAmountValue())
                .tossOrderId(order.getTossOrderId())
                .requestedAt(order.getCreatedAt())
                .build();
    }
}

