package com.dangdangsalon.domain.notification.repository;

import com.dangdangsalon.domain.notification.entity.FcmToken;
import com.dangdangsalon.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class FcmTokenRepositoryTest {

    @Autowired
    private FcmTokenRepository fcmTokenRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("FCM 토큰 저장 및 조회 테스트")
    void saveAndFindFcmToken() {
        // Given
        User user = User.builder().name("Test User").build();
        em.persist(user);

        FcmToken token = FcmToken.builder()
                .fcmToken("test-token")
                .user(user)
                .lastUserAt(LocalDateTime.now())
                .build();

        // When
        fcmTokenRepository.save(token);

        // Then
        Optional<FcmToken> foundToken = fcmTokenRepository.findByFcmToken("test-token");
        assertThat(foundToken).isPresent();
        assertThat(foundToken.get().getUser().getName()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("사용자 ID로 FCM 토큰 조회 테스트")
    void findByUserId() {
        // Given
        User user = User.builder().name("User with Token").build();
        em.persist(user);

        FcmToken token = FcmToken.builder()
                .fcmToken("user-token")
                .user(user)
                .lastUserAt(LocalDateTime.now())
                .build();

        em.persist(token);

        // When
        List<FcmToken> foundTokens = fcmTokenRepository.findByUserId(user.getId());

        // Then
        assertThat(foundTokens).isNotEmpty(); // List가 비어있지 않은지 확인
        assertThat(foundTokens.get(0).getFcmToken()).isEqualTo("user-token"); // 첫 번째 FCM 토큰의 값을 검증
    }


    @Test
    @DisplayName("FCM 토큰 삭제 테스트")
    void deleteByFcmToken() {
        // Given
        User user = User.builder().name("Delete User").build();
        em.persist(user);

        FcmToken token = FcmToken.builder()
                .fcmToken("delete-token")
                .user(user)
                .lastUserAt(LocalDateTime.now())
                .build();

        em.persist(token);

        // When
        fcmTokenRepository.deleteByFcmToken("delete-token");

        // Then
        Optional<FcmToken> deletedToken = fcmTokenRepository.findByFcmToken("delete-token");
        assertThat(deletedToken).isNotPresent();
    }

    @Test
    @DisplayName("존재하지 않는 FCM 토큰 조회 테스트")
    void findNonExistentFcmToken() {
        // When
        Optional<FcmToken> foundToken = fcmTokenRepository.findByFcmToken("non-existent-token");

        // Then
        assertThat(foundToken).isNotPresent();
    }
}
