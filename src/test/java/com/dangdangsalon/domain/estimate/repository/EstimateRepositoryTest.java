package com.dangdangsalon.domain.estimate.repository;

import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestRepository;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.groomerprofile.entity.ServiceType;
import com.dangdangsalon.domain.groomerprofile.repository.GroomerProfileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class EstimateRepositoryTest {

    @Autowired
    private EstimateRepository estimateRepository;

    @Autowired
    private EstimateRequestRepository estimateRequestRepository;

    @Autowired
    private GroomerProfileRepository groomerProfileRepository;

    @Test
    @DisplayName("Estimate ID로 GroomerProfile 포함 조회 - 성공")
    void findWithGroomerProfileById_Success() {
        // Given
        GroomerProfile groomerProfile = groomerProfileRepository.save(
                GroomerProfile.builder()
                        .name("이민수 미용사")
                        .build()
        );

        Estimate estimate = estimateRepository.save(
                Estimate.builder()
                        .groomerProfile(groomerProfile)
                        .build()
        );

        // When
        Optional<Estimate> result = estimateRepository.findWithGroomerProfileById(estimate.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getGroomerProfile().getName()).isEqualTo("이민수 미용사");
    }

    @Test
    @DisplayName("EstimateRequest로 Estimate 조회 - 성공")
    void findByEstimateRequest_Success() {
        // Given
        EstimateRequest estimateRequest = estimateRequestRepository.save(
                EstimateRequest.builder()
                        .serviceType(ServiceType.valueOf("ANY"))
                        .build()
        );

        estimateRepository.save(
                Estimate.builder()
                        .estimateRequest(estimateRequest)
                        .build()
        );

        estimateRepository.save(
                Estimate.builder()
                        .estimateRequest(estimateRequest)
                        .build()
        );

        // When
        Optional<List<Estimate>> result = estimateRepository.findByEstimateRequest(estimateRequest);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(2);
    }

    @Test
    @DisplayName("Estimate ID로 조회 - 성공")
    void findById_Success() {
        // Given
        Estimate estimate = estimateRepository.save(
                Estimate.builder()
                        .build()
        );

        // When
        Optional<Estimate> result = estimateRepository.findById(estimate.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(estimate.getId());
    }

    @Test
    @DisplayName("Estimate 저장 및 조회 - 성공")
    void saveAndRetrieveEstimate_Success() {
        // Given
        Estimate estimate = estimateRepository.save(
                Estimate.builder()
                        .description("Test Description")
                        .build()
        );

        // When
        Optional<Estimate> result = estimateRepository.findById(estimate.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getDescription()).isEqualTo("Test Description");
    }
}
