package com.dangdangsalon.domain.coupon.repository;

import com.dangdangsalon.domain.coupon.entity.CouponEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDateTime;
import java.util.List;

public interface CouponEventRepository extends JpaRepository<CouponEvent, Long> {
    // 1시간 이내에 시작하고, 아직 종료되지 않은 이벤트 조회
    CouponEvent findFirstByStartedAtBetweenAndEndedAtAfter(LocalDateTime start, LocalDateTime end, LocalDateTime now);

    @Query("SELECT e FROM CouponEvent e WHERE e.startedAt <= :now AND e.endedAt >= :now")
    List<CouponEvent> findActiveEvents(@Param("now") LocalDateTime now);
}
