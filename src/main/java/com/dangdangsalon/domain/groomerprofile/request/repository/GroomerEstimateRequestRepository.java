package com.dangdangsalon.domain.groomerprofile.request.repository;

import com.dangdangsalon.domain.groomerprofile.request.entity.GroomerEstimateRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroomerEstimateRequestRepository extends JpaRepository<GroomerEstimateRequest, Long> {
    List<GroomerEstimateRequest> findByGroomerProfileId(Long groomerProfileId);
}
