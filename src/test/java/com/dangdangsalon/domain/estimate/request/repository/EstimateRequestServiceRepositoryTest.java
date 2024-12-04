package com.dangdangsalon.domain.estimate.request.repository;

import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestProfiles;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestService;
import com.dangdangsalon.domain.groomerservice.entity.GroomerService;
import com.dangdangsalon.domain.groomerservice.repository.GroomerServiceRepository;
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

}
