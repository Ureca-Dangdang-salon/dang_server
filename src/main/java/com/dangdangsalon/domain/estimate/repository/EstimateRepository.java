package com.dangdangsalon.domain.estimate.repository;

import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EstimateRepository extends JpaRepository<Estimate, Long> {
    @Query("SELECT e FROM Estimate e JOIN FETCH e.groomerProfile gp WHERE e.id = :estimateId")
    Optional<Estimate> findWithGroomerProfileById(@Param("estimateId") Long estimateId);

    Optional<List<Estimate>> findByEstimateRequest(EstimateRequest estimateRequest);

    @Query("SELECT e FROM Estimate e JOIN FETCH e.groomerProfile gp JOIN FETCH e.estimateRequest er JOIN FETCH er.user WHERE e.id = :estimateId")
    Optional<Estimate> findWithGroomerProfileAndCustomerById(Long estimateId);
}
