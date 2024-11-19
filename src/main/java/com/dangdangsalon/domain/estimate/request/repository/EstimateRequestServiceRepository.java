package com.dangdangsalon.domain.estimate.request.repository;

import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstimateRequestServiceRepository extends JpaRepository<EstimateRequestService, Long> {
}
