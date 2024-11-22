package com.dangdangsalon.domain.estimate.request.repository;

import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestProfiles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EstimateRequestProfilesRepository extends JpaRepository<EstimateRequestProfiles, Long> {

    Optional<List<EstimateRequestProfiles>> findByEstimateRequest(EstimateRequest estimateRequest);

    Optional<EstimateRequestProfiles> findByEstimateRequestAndDogProfileId(EstimateRequest estimateRequest, Long dogProfileId);

    Optional<EstimateRequestProfiles> findByDogProfileIdAndEstimateRequestId(Long dogProfileId, Long estimateRequestId);

    Optional<List<EstimateRequestProfiles>> findByEstimateRequestId(Long estimateRequestId);
}
