package com.dangdangsalon.domain.estimate.request.repository;

import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestProfiles;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EstimateRequestServiceRepository extends JpaRepository<EstimateRequestService, Long> {
    Optional<List<EstimateRequestService>> findByEstimateRequestProfiles(EstimateRequestProfiles estimateRequestProfiles);
}
