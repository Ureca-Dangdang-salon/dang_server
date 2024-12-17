package com.dangdangsalon.domain.payment.repository;

import com.dangdangsalon.domain.orders.entity.Orders;
import com.dangdangsalon.domain.payment.entity.Payment;
import com.dangdangsalon.domain.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentKey(String paymentKey);

    Optional<Payment> findByOrders(Orders orders);

    List<Payment> findByPaymentStatus(PaymentStatus status);
}
