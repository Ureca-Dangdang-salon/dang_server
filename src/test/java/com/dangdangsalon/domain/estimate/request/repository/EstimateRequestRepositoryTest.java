package com.dangdangsalon.domain.estimate.request.repository;

import com.dangdangsalon.domain.dogprofile.repository.DogProfileRepository;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestProfiles;
import com.dangdangsalon.domain.dogprofile.entity.DogProfile;
import com.dangdangsalon.domain.groomerprofile.entity.ServiceType;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
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
class EstimateRequestRepositoryTest {

    @Autowired
    private EstimateRequestRepository estimateRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EstimateRequestProfilesRepository estimateRequestProfilesRepository;

    @Autowired
    private DogProfileRepository dogProfileRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("User ID로 EstimateRequest 조회 - 성공")
    void findByUserId_Success() {
        // Given
        User user = userRepository.save(
                User.builder()
                        .username("이민수")
                        .email("test@example.com")
                        .build()
        );

        DogProfile dogProfile = dogProfileRepository.save(
                DogProfile.builder()
                        .name("궁댕이")
                        .user(user)
                        .build()
        );

        EstimateRequest estimateRequest = estimateRequestRepository.save(
                EstimateRequest.builder()
                        .user(user)
                        .build()
        );

        estimateRequestProfilesRepository.save(
                EstimateRequestProfiles.builder()
                        .estimateRequest(estimateRequest)
                        .dogProfile(dogProfile)
                        .currentImageKey("imageKey1")
                        .build()
        );

        em.flush();
        em.clear();

        // When
        Optional<List<EstimateRequest>> result = estimateRequestRepository.findByUserId(user.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(1);
        assertThat(result.get().get(0).getUser().getUsername()).isEqualTo("이민수");
        assertThat(result.get().get(0).getEstimateRequestProfiles()).hasSize(1);
        assertThat(result.get().get(0).getEstimateRequestProfiles().get(0).getDogProfile().getName()).isEqualTo("궁댕이");
    }


    @Test
    @DisplayName("EstimateRequest 저장 및 조회 - 성공")
    void saveAndFindEstimateRequest_Success() {
        // Given
        User user = userRepository.save(
                User.builder()
                        .username("TestUser2")
                        .email("user2@example.com")
                        .build()
        );

        EstimateRequest estimateRequest = estimateRequestRepository.save(
                EstimateRequest.builder()
                        .user(user)
                        .serviceType(ServiceType.valueOf("ANY"))
                        .build()
        );

        // When
        Optional<EstimateRequest> result = estimateRequestRepository.findById(estimateRequest.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUser().getUsername()).isEqualTo("TestUser2");
        assertThat(result.get().getServiceType()).isEqualTo(ServiceType.ANY);
    }

    @Test
    @DisplayName("EstimateRequest 삭제 - 성공")
    void deleteEstimateRequest_Success() {
        // Given
        EstimateRequest estimateRequest = estimateRequestRepository.save(
                EstimateRequest.builder()
                        .serviceType(ServiceType.valueOf("ANY"))
                        .build()
        );

        // When
        estimateRequestRepository.delete(estimateRequest);
        Optional<EstimateRequest> result = estimateRequestRepository.findById(estimateRequest.getId());

        // Then
        assertThat(result).isNotPresent();
    }
}
