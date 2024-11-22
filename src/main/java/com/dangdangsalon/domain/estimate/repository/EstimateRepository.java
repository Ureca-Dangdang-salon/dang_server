package com.dangdangsalon.domain.estimate.repository;

import com.dangdangsalon.domain.estimate.entity.Estimate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstimateRepository extends JpaRepository<Estimate, Long> {
}
