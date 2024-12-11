package com.dangdangsalon.domain.coupon.repository;

import com.dangdangsalon.domain.coupon.entity.CouponEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CouponEventRepository extends JpaRepository<CouponEvent, Long> {

    @Query("SELECT e FROM CouponEvent e WHERE e.startedAt <= :now AND e.endedAt >= :now")
    Optional<CouponEvent> findActiveEvent(@Param("now") LocalDateTime now);

    @Query("SELECT e FROM CouponEvent e WHERE e.name = :name AND e.startedAt <= :now AND e.endedAt >= :now")
    Optional<CouponEvent> findActiveEventByNameAndNow(@Param("name") String name, @Param("now") LocalDateTime now);

    Optional<CouponEvent> findByName(String name);
}
