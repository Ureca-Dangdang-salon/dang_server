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
    private Long id;

    private String name;

    private int discountAmount;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private int totalQuantity;

    private int remainQuantity;

    @Enumerated(EnumType.STRING)
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
