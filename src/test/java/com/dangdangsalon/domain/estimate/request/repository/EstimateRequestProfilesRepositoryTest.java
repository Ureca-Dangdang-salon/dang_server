package com.dangdangsalon.domain.estimate.request.repository;

import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestProfiles;
import com.dangdangsalon.domain.groomerprofile.entity.ServiceType;
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
class EstimateRequestProfilesRepositoryTest {

    @Autowired
    private EstimateRequestProfilesRepository repository;

    @Autowired
    private EstimateRequestRepository estimateRequestRepository;

    @Test
    @DisplayName("EstimateRequest로 EstimateRequestProfiles 조회 - 성공")
    void findByEstimateRequest_Success() {
        // Given
        EstimateRequest estimateRequest = estimateRequestRepository.save(
                EstimateRequest.builder()
                        .serviceType(ServiceType.valueOf("ANY"))
                        .build()
        );

        EstimateRequestProfiles profiles = repository.save(
                EstimateRequestProfiles.builder()
                        .estimateRequest(estimateRequest)
                        .dogProfile(null) // DogProfile은 생성하지 않음
                        .currentImageKey("imageKey1")
                        .aggression(true)
                        .healthIssue(false)
                        .description("설명1")
                        .build()
        );

        // When
        Optional<List<EstimateRequestProfiles>> result = repository.findByEstimateRequest(estimateRequest);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(1);
        assertThat(result.get().get(0).getCurrentImageKey()).isEqualTo("imageKey1");
    }

    @Test
    @DisplayName("EstimateRequest와 DogProfileId로 조회 - 성공")
    void findByEstimateRequestAndDogProfileId_Success() {
        // Given
        EstimateRequest estimateRequest = estimateRequestRepository.save(
                EstimateRequest.builder()
                        .serviceType(ServiceType.valueOf("ANY"))
                        .build()
        );

        EstimateRequestProfiles profiles = repository.save(
                EstimateRequestProfiles.builder()
                        .estimateRequest(estimateRequest)
                        .dogProfile(null) // DogProfile은 필요에 따라 설정
                        .currentImageKey("imageKey2")
                        .build()
        );

        // When
        Optional<EstimateRequestProfiles> result = repository.findByEstimateRequestAndDogProfileId(estimateRequest, null);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCurrentImageKey()).isEqualTo("imageKey2");
    }

    @Test
    @DisplayName("EstimateRequestId로 Profiles 목록 조회 - 성공")
    void findByEstimateRequestId_Success() {
        // Given
        EstimateRequest estimateRequest = estimateRequestRepository.save(
                EstimateRequest.builder()
                        .serviceType(ServiceType.valueOf("ANY"))
                        .build()
        );

        repository.save(
                EstimateRequestProfiles.builder()
                        .estimateRequest(estimateRequest)
                        .currentImageKey("imageKey3")
                        .aggression(false)
                        .healthIssue(true)
                        .description("설명2")
                        .build()
        );

        repository.save(
                EstimateRequestProfiles.builder()
                        .estimateRequest(estimateRequest)
                        .currentImageKey("imageKey4")
                        .build()
        );

        // When
        Optional<List<EstimateRequestProfiles>> result = repository.findByEstimateRequestId(estimateRequest.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(2);
        assertThat(result.get().get(0).getCurrentImageKey()).isEqualTo("imageKey3");
        assertThat(result.get().get(1).getCurrentImageKey()).isEqualTo("imageKey4");
    }

    @Test
    @DisplayName("Charges 업데이트 테스트")
    void updateCharges_Success() {
        // Given
        EstimateRequest estimateRequest = estimateRequestRepository.save(
                EstimateRequest.builder()
                        .serviceType(ServiceType.valueOf("ANY"))
                        .build()
        );

        EstimateRequestProfiles profiles = repository.save(
                EstimateRequestProfiles.builder()
                        .estimateRequest(estimateRequest)
                        .currentImageKey("imageKey1")
                        .aggressionCharge(1000)
                        .healthIssueCharge(2000)
                        .build()
        );

        // When
        profiles.updateCharges(3000, 4000);
        repository.save(profiles);

        // Then
        Optional<EstimateRequestProfiles> updatedProfile = repository.findById(profiles.getId());
        assertThat(updatedProfile).isPresent();
        assertThat(updatedProfile.get().getAggressionCharge()).isEqualTo(3000);
        assertThat(updatedProfile.get().getHealthIssueCharge()).isEqualTo(4000);
    }
}
