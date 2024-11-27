package com.dangdangsalon.domain.groomerprofile.repository;

import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class GroomerProfileRepositoryTest {

    @Autowired
    private GroomerProfileRepository groomerProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("GroomerProfile 저장 및 조회 - 성공")
    void saveAndFindById_Success() {
        // Given
        User user = userRepository.save(
                User.builder()
                        .username("이민수")
                        .email("test@example.com")
                        .build()
        );

        GroomerProfile groomerProfile = groomerProfileRepository.save(
                GroomerProfile.builder()
                        .name("나는 미용사")
                        .phone("010-1234-5678")
                        .user(user)
                        .build()
        );

        // When
        Optional<GroomerProfile> result = groomerProfileRepository.findById(groomerProfile.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("나는 미용사");
        assertThat(result.get().getPhone()).isEqualTo("010-1234-5678");
        assertThat(result.get().getUser().getUsername()).isEqualTo("이민수");
    }

    @Test
    @DisplayName("GroomerProfile 삭제 - 성공")
    void deleteGroomerProfile_Success() {
        // Given
        User user = userRepository.save(
                User.builder()
                        .username("삭제 되기 싫은 유저")
                        .email("delete@example.com")
                        .build()
        );

        GroomerProfile groomerProfile = groomerProfileRepository.save(
                GroomerProfile.builder()
                        .name("삭제 되기 싫은 미용사")
                        .phone("010-8765-4321")
                        .user(user)
                        .build()
        );

        // When
        groomerProfileRepository.delete(groomerProfile);
        Optional<GroomerProfile> result = groomerProfileRepository.findById(groomerProfile.getId());

        // Then
        assertThat(result).isEmpty();
    }

}
