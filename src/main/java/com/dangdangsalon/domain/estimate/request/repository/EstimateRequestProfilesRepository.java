package com.dangdangsalon.domain.estimate.request.repository;

import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestProfiles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstimateRequestProfilesRepository extends JpaRepository<EstimateRequestProfiles, Long> {
}
