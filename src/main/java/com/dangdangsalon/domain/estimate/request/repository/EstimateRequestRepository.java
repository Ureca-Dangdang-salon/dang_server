package com.dangdangsalon.domain.estimate.request.repository;

import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EstimateRequestRepository extends JpaRepository<EstimateRequest, Long> {

    // 내 견적 요청 조회 시 n+1 문제 해결
    @Query("SELECT er FROM EstimateRequest er " +
            "JOIN FETCH er.estimateRequestProfiles erp " +
            "JOIN FETCH erp.dogProfile dp " +
            "WHERE er.user.id = :userId")
    Optional<List<EstimateRequest>> findByUserId(@Param("userId") Long userId);
}
