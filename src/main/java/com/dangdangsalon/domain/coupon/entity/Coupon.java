package com.dangdangsalon.domain.coupon.entity;

import com.dangdangsalon.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Enumerated(EnumType.STRING)
    private CouponStatus status;

    private String couponName;

    private int discountAmount;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public Coupon(LocalDateTime expiredAt, CouponStatus status, String couponName, int discountAmount,
                  DiscountType discountType, User user) {
        this.expiredAt = expiredAt;
        this.status = status;
        this.couponName = couponName;
        this.discountAmount = discountAmount;
        this.discountType = discountType;
        this.user = user;
    }
}
