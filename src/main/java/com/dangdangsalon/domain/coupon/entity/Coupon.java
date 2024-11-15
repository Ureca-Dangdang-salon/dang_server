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
    @Column(name = "coupon_user_id")
    private Long id;

    private LocalDateTime expired_at;

    private CouponStatus status;

    @ManyToOne
    @JoinColumn(name = "coupon_id", nullable = false)
    private CouponEvent couponEvent;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public Coupon(LocalDateTime expired_at, CouponStatus status, CouponEvent couponEvent, User user) {
        this.expired_at = expired_at;
        this.status = status;
        this.couponEvent = couponEvent;
        this.user = user;
    }
}
