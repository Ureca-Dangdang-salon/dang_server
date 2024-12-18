package com.dangdangsalon.domain.groomerprofile.request.repository;

import com.dangdangsalon.domain.groomerprofile.request.entity.GroomerEstimateRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GroomerEstimateRequestRepository extends JpaRepository<GroomerEstimateRequest, Long> {
    Optional<List<GroomerEstimateRequest>> findByGroomerProfileId(Long groomerProfileId);

    Optional<GroomerEstimateRequest> findByEstimateRequestIdAndGroomerProfileId(Long estimateRequestId, Long groomerProfileId);

    @Modifying
    @Query("DELETE FROM GroomerEstimateRequest g WHERE g.estimateRequest.requestDate < :beforeDate")
    void deleteByRequestDateBefore(LocalDateTime beforeDate);
}
