package com.dangdangsalon.domain.orders.repository;

import com.dangdangsalon.domain.orders.entity.OrderStatus;
import com.dangdangsalon.domain.orders.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
    Optional<Orders> findByEstimateId(Long estimateId);
    Optional<Orders> findByTossOrderId(String tossOrderId);
    Optional<List<Orders>> findAllByUserIdAndStatus(Long userId, OrderStatus status);
}
