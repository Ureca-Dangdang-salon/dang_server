package com.dangdangsalon.domain.groomerservice.repository;

import com.dangdangsalon.domain.groomerservice.entity.GroomerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class GroomerServiceRepositoryTest {

    @Autowired
    private GroomerServiceRepository groomerServiceRepository;

    @Test
    @DisplayName("GroomerService 저장 및 조회 - 성공")
    void saveAndFindById_Success() {
        // Given
        GroomerService groomerService = groomerServiceRepository.save(
                GroomerService.builder()
                        .description("목욕")
                        .isCustom(false)
                        .build()
        );

        // When
        Optional<GroomerService> result = groomerServiceRepository.findById(groomerService.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getDescription()).isEqualTo("목욕");
        assertThat(result.get().getIsCustom()).isFalse();
    }

    @Test
    @DisplayName("GroomerService 삭제 - 성공")
    void deleteGroomerService_Success() {
        // Given
        GroomerService groomerService = groomerServiceRepository.save(
                GroomerService.builder()
                        .description("특별 머드팩 서비스")
                        .isCustom(true)
                        .build()
        );

        // When
        groomerServiceRepository.delete(groomerService);
        Optional<GroomerService> result = groomerServiceRepository.findById(groomerService.getId());

        // Then
        assertThat(result).isEmpty();
    }
}
