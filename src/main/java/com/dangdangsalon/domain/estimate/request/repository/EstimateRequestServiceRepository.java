package com.dangdangsalon.domain.estimate.request.repository;

import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestProfiles;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestService;
import com.dangdangsalon.domain.groomerservice.entity.GroomerService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EstimateRequestServiceRepository extends JpaRepository<EstimateRequestService, Long> {

    @Query("SELECT ers FROM EstimateRequestService ers " +
            "JOIN FETCH ers.groomerService " +
            "WHERE ers.estimateRequestProfiles = :estimateRequestProfiles")
    Optional<List<EstimateRequestService>> findByEstimateRequestProfiles(@Param("estimateRequestProfiles") EstimateRequestProfiles estimateRequestProfiles);

    Optional<EstimateRequestService> findByEstimateRequestProfilesAndGroomerService(EstimateRequestProfiles estimateRequestProfiles, GroomerService groomerService);

    List<EstimateRequestService> findByEstimateRequestProfilesId(Long estimateRequestId);

    @Query("SELECT s FROM EstimateRequestService s WHERE s.estimateRequestProfiles.id IN :profileIds")
    List<EstimateRequestService> findByEstimateRequestServicesProfilesIdIn(List<Long> profileIds);
}
