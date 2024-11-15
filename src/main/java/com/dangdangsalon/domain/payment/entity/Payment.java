package com.dangdangsalon.domain.payment.entity;

import com.dangdangsalon.domain.orders.entity.Orders;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Column(name = "payment_key")
    private String paymentKey;

    @Column(name = "total_amount")
    private int totalAmount;

    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    private String method;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Orders orders;

    @Builder
    public Payment(String paymentKey, int totalAmount, PaymentStatus paymentStatus,
                   LocalDateTime requestedAt, String method, Orders orders) {
        this.paymentKey = paymentKey;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.requestedAt = requestedAt;
        this.method = method;
        this.orders = orders;
    }
}
