package com.dangdangsalon.domain.groomerprofile.repository;

import com.dangdangsalon.domain.groomerprofile.entity.GroomerCanService;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.groomerservice.entity.GroomerService;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class GroomerCanServiceRepositoryTest {

    @Autowired
    private GroomerCanServiceRepository groomerCanServiceRepository;

    @Autowired
    private GroomerProfileRepository groomerProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.dangdangsalon.domain.groomerservice.repository.GroomerServiceRepository groomerServiceRepository;

    @Test
    @DisplayName("GroomerProfile로 GroomerCanService 조회 - 성공")
    void findByGroomerProfile_Success() {
        // Given
        User user = userRepository.save(
                User.builder()
                        .username("이민수")
                        .email("test@example.com")
                        .build()
        );

        GroomerProfile groomerProfile = groomerProfileRepository.save(
                GroomerProfile.builder()
                        .name("미용사 호소인")
                        .phone("010-1234-5678")
                        .user(user)
                        .build()
        );

        GroomerService groomerService1 = groomerServiceRepository.save(
                GroomerService.builder()
                        .description("목욕")
                        .isCustom(false)
                        .build()
        );

        GroomerService groomerService2 = groomerServiceRepository.save(
                GroomerService.builder()
                        .description("부분 미용")
                        .isCustom(true)
                        .build()
        );

        groomerCanServiceRepository.save(
                GroomerCanService.builder()
                        .groomerProfile(groomerProfile)
                        .groomerService(groomerService1)
                        .build()
        );

        groomerCanServiceRepository.save(
                GroomerCanService.builder()
                        .groomerProfile(groomerProfile)
                        .groomerService(groomerService2)
                        .build()
        );

        // When
        List<GroomerCanService> result = groomerCanServiceRepository.findByGroomerProfile(groomerProfile);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("groomerService.description")
                .containsExactlyInAnyOrder("목욕", "부분 미용");
    }

    @Test
    @DisplayName("GroomerProfile가 없는 경우 GroomerCanService 조회 - 빈 결과 반환")
    void findByGroomerProfile_NoServices() {
        // Given
        User user = userRepository.save(
                User.builder()
                        .username("이도현")
                        .email("empty@example.com")
                        .build()
        );

        GroomerProfile groomerProfile = groomerProfileRepository.save(
                GroomerProfile.builder()
                        .name("미용사 호소인")
                        .phone("010-9876-5432")
                        .user(user)
                        .build()
        );

        // When
        List<GroomerCanService> result = groomerCanServiceRepository.findByGroomerProfile(groomerProfile);

        // Then
        assertThat(result).isEmpty();
    }
}
