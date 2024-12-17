package com.dangdangsalon.domain.orders.repository;

import com.dangdangsalon.domain.orders.entity.OrderStatus;
import com.dangdangsalon.domain.orders.entity.Orders;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
    Optional<Orders> findByEstimateId(Long estimateId);

    Optional<Orders> findByTossOrderId(String tossOrderId);

    Optional<List<Orders>> findAllByUserIdAndStatusNot(Long userId, OrderStatus status);

    @Query("SELECT o FROM Orders o JOIN o.estimate e WHERE o.user.id = :userId AND o.status = :status AND e.date BETWEEN :contestStartDate AND :contestEndDate")
    Optional<List<Orders>> findAllByUserIdAndStatusAndContestDate(Long userId, OrderStatus status,
                                                                  LocalDateTime contestStartDate,
                                                                  LocalDateTime contestEndDate);
}
