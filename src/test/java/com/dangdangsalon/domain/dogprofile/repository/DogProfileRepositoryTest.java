package com.dangdangsalon.domain.dogprofile.repository;

import com.dangdangsalon.domain.dogprofile.entity.DogProfile;
import com.dangdangsalon.domain.dogprofile.entity.DogProfileFeature;
import com.dangdangsalon.domain.dogprofile.entity.Gender;
import com.dangdangsalon.domain.dogprofile.feature.entity.Feature;
import com.dangdangsalon.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
class DogProfileRepositoryTest {

    @Autowired
    private DogProfileRepository dogProfileRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user1;
    private User user2;
    private DogProfile dogProfile;

    @BeforeEach
    void setUp() {
        // 테스트용 유저 생성
        user1 = User.builder()
                .email("test1@example.com")
                .name("홍길동")
                .build();

        user2 = User.builder()
                .email("test2@example.com")
                .name("이순신")
                .build();

        dogProfile = DogProfile.builder()
                .name("Buddy")
                .species("Golden Retriever")
                .gender(Gender.MALE)
                .user(user1)
                .build();
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(dogProfile);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("특정 유저의 강아지 프로필을 조회한다")
    void testFindByUser() {
        // given
        DogProfile dog1 = createDogProfile("dog1.jpg", "바둑이", user1);
        DogProfile dog2 = createDogProfile("dog2.jpg", "쫑이", user1);
        DogProfile dog3 = createDogProfile("dog3.jpg", "백구", user2);

        entityManager.persist(dog1);
        entityManager.persist(dog2);
        entityManager.persist(dog3);
        entityManager.flush();
        entityManager.clear();

        // when
        Optional<List<DogProfile>> foundProfilesOpt = dogProfileRepository.findByUser(user1);

        // then
        assertThat(foundProfilesOpt).isPresent();
        List<DogProfile> foundProfiles = foundProfilesOpt.get();
        assertThat(foundProfiles).hasSize(3)
                .extracting("name")
                .containsExactlyInAnyOrder("바둑이", "쫑이", "Buddy");
    }

    @Test
    @DisplayName("강아지 프로필이 없는 유저의 경우 빈 결과를 반환한다")
    void testFindByUser_NoProfiles() {
        // when
        Optional<List<DogProfile>> foundProfilesOpt = dogProfileRepository.findByUser(user2);

        // then
        assertThat(foundProfilesOpt).isPresent();
        assertThat(foundProfilesOpt.get()).isEmpty();
    }

    @Test
    @DisplayName("강아지 프로필을 저장하고 조회한다")
    void testSaveDogProfile() {
        // given
        DogProfile dogProfile = createDogProfile("dog1.jpg", "멍멍이", user1);

        // when
        DogProfile savedProfile = dogProfileRepository.save(dogProfile);

        // then
        assertThat(savedProfile.getId()).isNotNull();
        assertThat(savedProfile.getName()).isEqualTo("멍멍이");
        assertThat(savedProfile.getImageKey()).isEqualTo("dog1.jpg");
        assertThat(savedProfile.getUser()).isEqualTo(user1);
    }

    @Test
    @DisplayName("ID와 사용자 ID로 강아지 프로필 조회 및 관련 Feature 포함 - 성공 (EntityManager 사용)")
    void testFindByIdAndUserIdWithFeaturesUsingEntityManager() {

        // Feature를 영속화
        Feature feature = new Feature("Cute", false);
        entityManager.persist(feature); // Feature를 영속화

        // DogProfileFeature 생성 및 연결
        DogProfileFeature dogProfileFeature = new DogProfileFeature(feature, dogProfile);
        entityManager.persist(dogProfileFeature);
        dogProfile.getDogProfileFeatures().add(dogProfileFeature);

        // When
        Optional<DogProfile> foundProfile = dogProfileRepository.findByIdAndUserIdWithFeatures(dogProfile.getId(), user1.getId());

        // Then
        assertThat(foundProfile).isPresent();
        assertThat(foundProfile.get().getDogProfileFeatures()).hasSize(1);
        assertThat(foundProfile.get().getDogProfileFeatures().get(0).getFeature().getDescription()).isEqualTo("Cute");
    }

    @Test
    @DisplayName("ID와 사용자 ID로 강아지 프로필 조회 실패 - 프로필 없음")
    void testFindByIdAndUserIdWithFeatures_notFound() {
        // Given
        Long invalidProfileId = 999L;
        Long invalidUserId = 999L;

        // When
        Optional<DogProfile> foundProfile = dogProfileRepository.findByIdAndUserIdWithFeatures(invalidProfileId, invalidUserId);

        // Then
        assertThat(foundProfile).isEmpty();
    }

    private DogProfile createDogProfile(String imageKey, String name, User user) {
        return DogProfile.builder()
                .imageKey(imageKey)
                .name(name)
                .user(user)
                .build();
    }
}
