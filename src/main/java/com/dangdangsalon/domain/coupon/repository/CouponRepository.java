package com.dangdangsalon.domain.coupon.repository;

import com.dangdangsalon.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
}
