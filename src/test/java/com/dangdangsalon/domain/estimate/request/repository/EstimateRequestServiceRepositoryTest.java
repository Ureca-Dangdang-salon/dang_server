package com.dangdangsalon.domain.estimate.request.repository;

import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestProfiles;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestService;
import com.dangdangsalon.domain.groomerservice.entity.GroomerService;
import com.dangdangsalon.domain.groomerservice.repository.GroomerServiceRepository;
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
class EstimateRequestServiceRepositoryTest {

    @Autowired
    private EstimateRequestServiceRepository estimateRequestServiceRepository;

    @Autowired
    private EstimateRequestProfilesRepository estimateRequestProfilesRepository;

    @Autowired
    private GroomerServiceRepository groomerServiceRepository;
    
    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("EstimateRequestProfiles로 EstimateRequestService 조회 - 성공")
    void findByEstimateRequestProfiles_Success() {
        // Given
        EstimateRequestProfiles estimateRequestProfiles = estimateRequestProfilesRepository.save(
                EstimateRequestProfiles.builder()
                        .description("확 물어버린다 으앙")
                        .build()
        );

        GroomerService groomerService = groomerServiceRepository.save(
                GroomerService.builder()
                        .description("목욕")
                        .isCustom(false)
                        .build()
        );

        estimateRequestServiceRepository.save(
                EstimateRequestService.builder()
                        .estimateRequestProfiles(estimateRequestProfiles)
                        .groomerService(groomerService)
                        .build()
        );

        // When
        Optional<List<EstimateRequestService>> result = estimateRequestServiceRepository.findByEstimateRequestProfiles(estimateRequestProfiles);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(1);
        assertThat(result.get().get(0).getGroomerService().getDescription()).isEqualTo("목욕");
    }


    @Test
    @DisplayName("EstimateRequestProfiles와 GroomerService로 EstimateRequestService 조회 - 성공")
    void findByEstimateRequestProfilesAndGroomerService_Success() {
        // Given
        EstimateRequestProfiles estimateRequestProfiles = estimateRequestProfilesRepository.save(
                EstimateRequestProfiles.builder()
                        .description("확 물어버린다 으앙")
                        .build()
        );

        GroomerService groomerService = groomerServiceRepository.save(
                GroomerService.builder()
                        .description("털 미용")
                        .isCustom(false)
                        .build()
        );

        EstimateRequestService estimateRequestService = estimateRequestServiceRepository.save(
                EstimateRequestService.builder()
                        .estimateRequestProfiles(estimateRequestProfiles)
                        .groomerService(groomerService)
                        .build()
        );
        // When
        Optional<EstimateRequestService> result = estimateRequestServiceRepository.findByEstimateRequestProfilesAndGroomerService(estimateRequestProfiles, groomerService);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getGroomerService().getDescription()).isEqualTo("털 미용");
    }

    @Test
    @DisplayName("EstimateRequestService 저장 및 조회 - 성공")
    void saveAndRetrieve_Success() {
        // Given
        EstimateRequestProfiles estimateRequestProfiles = estimateRequestProfilesRepository.save(
                EstimateRequestProfiles.builder()
                        .description("확 물어버린다 으앙")
                        .build()
        );

        GroomerService groomerService = groomerServiceRepository.save(
                GroomerService.builder()
                        .description("발톱 정리")
                        .isCustom(true)
                        .build()
        );

        EstimateRequestService savedService = estimateRequestServiceRepository.save(
                EstimateRequestService.builder()
                        .estimateRequestProfiles(estimateRequestProfiles)
                        .groomerService(groomerService)
                        .build()
        );

        // When
        Optional<EstimateRequestService> result = estimateRequestServiceRepository.findById(savedService.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getGroomerService().getDescription()).isEqualTo("발톱 정리");
    }

    @Test
    @DisplayName("프로필 ID 목록으로 EstimateRequestService 조회 테스트")
    void testFindByEstimateRequestServicesProfilesIdIn() {
        GroomerService groomerServiceA = GroomerService.builder()
                .description("Service A")
                .build();
        em.persist(groomerServiceA);

        GroomerService groomerServiceB = GroomerService.builder()
                .description("Service B")
                .build();
        em.persist(groomerServiceB);

        EstimateRequestProfiles profile1 = EstimateRequestProfiles.builder().build();
        em.persist(profile1);

        EstimateRequestProfiles profile2 = EstimateRequestProfiles.builder().build();
        em.persist(profile2);

        EstimateRequestService serviceA = EstimateRequestService.builder()
                .estimateRequestProfiles(profile1)
                .groomerService(groomerServiceA)
                .build();
        em.persist(serviceA);

        EstimateRequestService serviceB = EstimateRequestService.builder()
                .estimateRequestProfiles(profile2)
                .groomerService(groomerServiceB)
                .build();
        em.persist(serviceB);

        em.flush();

        List<Long> profileIds = List.of(profile1.getId(), profile2.getId());
        List<EstimateRequestService> services =
                estimateRequestServiceRepository.findByEstimateRequestServicesProfilesIdIn(profileIds);

        assertThat(services).hasSize(2);
        assertThat(services).extracting("groomerService.description")
                .containsExactlyInAnyOrder("Service A", "Service B");
    }
}
