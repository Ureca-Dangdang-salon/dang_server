package com.dangdangsalon.domain.estimate.repository;

import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.entity.EstimateStatus;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EstimateRepository extends JpaRepository<Estimate, Long> {
    @Query("SELECT e FROM Estimate e JOIN FETCH e.groomerProfile gp WHERE e.id = :estimateId")
    Optional<Estimate> findWithGroomerProfileById(@Param("estimateId") Long estimateId);

    Optional<List<Estimate>> findByEstimateRequest(EstimateRequest estimateRequest);

    @Query("SELECT e FROM Estimate e JOIN FETCH e.groomerProfile gp JOIN FETCH e.estimateRequest er JOIN FETCH er.user WHERE e.id = :estimateId")
    Optional<Estimate> findWithGroomerProfileAndCustomerById(Long estimateId);

    @Query("SELECT e FROM Estimate e WHERE e.date BETWEEN :start AND :end")
    List<Estimate> findReservationsForTomorrow(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT e FROM Estimate e " +
            "JOIN FETCH e.groomerProfile gp " +
            "JOIN FETCH e.estimateRequest er " +
            "JOIN FETCH er.user u " +
            "WHERE e.id = :estimateId")
    Optional<Estimate> findWithEstimateById(@Param("estimateId") Long estimateId);

    Optional<Estimate> findByEstimateRequestId(Long requestId);

    Optional<Estimate> findByEstimateRequestIdAndGroomerProfileId(Long estimateRequestId, Long groomerProfileId);
}
