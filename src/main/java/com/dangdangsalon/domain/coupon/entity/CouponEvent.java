package com.dangdangsalon.domain.coupon.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long id;

    private String name;

    @Column(name = "discount_amount")
    private int discountAmount;

    @Column(name = "discount_type")
    private DiscountType discountType;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "total_quantity")
    private int totalQuantity;

    @Column(name = "remain_quantity")
    private int remainQuantity;

    private CouponEventStatus status;

    @Builder
    public CouponEvent(String name, int discountAmount, DiscountType discountType, LocalDateTime startedAt,
                       LocalDateTime endedAt, int totalQuantity, int remainQuantity, CouponEventStatus status) {
        this.name = name;
        this.discountAmount = discountAmount;
        this.discountType = discountType;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.totalQuantity = totalQuantity;
        this.remainQuantity = remainQuantity;
        this.status = status;
    }
}
