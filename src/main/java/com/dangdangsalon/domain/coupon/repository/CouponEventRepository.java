package com.dangdangsalon.domain.coupon.repository;

import com.dangdangsalon.domain.coupon.entity.CouponEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CouponEventRepository extends JpaRepository<CouponEvent, Long> {
    @Query("SELECT e FROM CouponEvent e WHERE e.startedAt <= :now AND e.endedAt >= :now")
    List<CouponEvent> findActiveEvents(@Param("now") LocalDateTime now);

    @Query("SELECT e FROM CouponEvent e WHERE e.startedAt > CURRENT_TIMESTAMP OR e.endedAt > CURRENT_TIMESTAMP")
    List<CouponEvent> findUpcomingEvents();
}
