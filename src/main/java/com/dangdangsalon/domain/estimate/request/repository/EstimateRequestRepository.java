package com.dangdangsalon.domain.estimate.request.repository;

import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstimateRequestRepository extends JpaRepository<EstimateRequest, Long> {
}
