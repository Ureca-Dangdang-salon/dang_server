package com.dangdangsalon.domain.payment.entity;

import com.dangdangsalon.config.base.BaseEntity;
import com.dangdangsalon.domain.coupon.entity.Coupon;
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
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_key")
    private String paymentKey;

    @Column(name = "total_amount")
    private int totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    private String paymentMethod; //Enum 고민.

    @OneToOne
    @JoinColumn(name = "order_id")
    private Orders orders;

    @ManyToOne
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @Builder
    public Payment(String paymentKey, int totalAmount, PaymentStatus paymentStatus,
                   LocalDateTime requestedAt, String paymentMethod, Orders orders, Coupon coupon) {
        this.paymentKey = paymentKey;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.requestedAt = requestedAt;
        this.paymentMethod = paymentMethod;
        this.orders = orders;
        this.coupon = coupon;
    }

    public void updatePaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
