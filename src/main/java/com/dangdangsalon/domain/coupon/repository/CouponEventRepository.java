package com.dangdangsalon.domain.coupon.repository;

import com.dangdangsalon.domain.coupon.entity.CouponEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CouponEventRepository extends JpaRepository<CouponEvent, Long> {
    @Query("SELECT e FROM CouponEvent e WHERE e.startedAt <= :now AND e.endedAt >= :now")
    List<CouponEvent> findActiveEvent(@Param("now") LocalDateTime now);
}
