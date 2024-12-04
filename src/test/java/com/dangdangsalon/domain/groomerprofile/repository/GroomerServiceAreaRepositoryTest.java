package com.dangdangsalon.domain.groomerprofile.repository;

import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerServiceArea;
import com.dangdangsalon.domain.region.entity.District;
import com.dangdangsalon.domain.region.repository.DistrictRepository;
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
class GroomerServiceAreaRepositoryTest {

    @Autowired
    private GroomerServiceAreaRepository groomerServiceAreaRepository;

    @Autowired
    private DistrictRepository districtRepository;

    @Autowired
    private GroomerProfileRepository groomerProfileRepository;

    @Test
    @DisplayName("District로 GroomerServiceArea 조회 - 성공")
    void findByDistrict_Success() {
        // Given
        District district = districtRepository.save(
                District.builder()
                        .name("강남구")
                        .build()
        );

        GroomerProfile groomerProfile = groomerProfileRepository.save(
                GroomerProfile.builder()
                        .name("미요미요")
                        .phone("010-1234-5678")
                        .build()
        );

        groomerServiceAreaRepository.save(
                GroomerServiceArea.builder()
                        .district(district)
                        .groomerProfile(groomerProfile)
                        .build()
        );

        // When
        Optional<List<GroomerServiceArea>> result = groomerServiceAreaRepository.findByDistrict(district);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(1);
        assertThat(result.get().get(0).getDistrict().getName()).isEqualTo("강남구");
        assertThat(result.get().get(0).getGroomerProfile().getName()).isEqualTo("미요미요");
    }

    @Test
    @DisplayName("District에 매핑된 GroomerServiceArea가 없는 경우 - 빈 결과 반환")
    void findByDistrict_NoResults() {
        // Given
        District district = districtRepository.save(
                District.builder()
                        .name("강남구")
                        .build()
        );

        // When
        Optional<List<GroomerServiceArea>> result = groomerServiceAreaRepository.findByDistrict(district);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEmpty();
    }
}
