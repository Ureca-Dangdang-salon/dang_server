package com.dangdangsalon.domain.groomerprofile.request.repository;

import com.dangdangsalon.domain.groomerprofile.request.entity.GroomerEstimateRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroomerEstimateRequestRepository extends JpaRepository<GroomerEstimateRequest, Long> {
    Optional<List<GroomerEstimateRequest>> findByGroomerProfileId(Long groomerProfileId);

    Optional<GroomerEstimateRequest> findByEstimateRequestIdAndGroomerProfileId(Long estimateRequestId, Long groomerProfileId);
}
