package com.dangdangsalon.domain.coupon.repository;

import com.dangdangsalon.domain.coupon.entity.CouponEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponEventRepository extends JpaRepository<CouponEvent, Long> {
}
