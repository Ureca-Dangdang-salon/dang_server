package com.dangdangsalon.domain.groomerprofile.repository;

import com.dangdangsalon.domain.groomerprofile.entity.*;
import com.dangdangsalon.domain.groomerservice.entity.GroomerService;
import com.dangdangsalon.domain.mypage.dto.res.BadgeResponseDto;
import com.dangdangsalon.domain.mypage.dto.res.DistrictResponseDto;
import com.dangdangsalon.domain.mypage.dto.res.GroomerServicesResponseDto;
import com.dangdangsalon.domain.region.entity.City;
import com.dangdangsalon.domain.region.entity.District;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
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
class GroomerProfileRepositoryTest {

    @Autowired
    private GroomerProfileRepository groomerProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User user;
    private GroomerProfile groomerProfile;
    private District district;

    @BeforeEach
    void setUp() {
        // City and District setup
        City city = new City("서울시");
        district = new District("종로구", city);
        entityManager.persist(city);
        entityManager.persist(district);

        // User setup
        user = User.builder()
                .email("test@example.com")
                .name("Tester")
                .district(district)
                .build();
        entityManager.persist(user);

        // GroomerProfile setup
        groomerProfile = GroomerProfile.builder().user(user).build();
        entityManager.persist(groomerProfile);
        entityManager.flush();
        entityManager.clear();
    }
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

    @Test
    @DisplayName("사용자 ID로 GroomerProfile 조회")
    void testFindByUserId() {
        Optional<GroomerProfile> foundProfile = groomerProfileRepository.findByUserIdWithDistrict(user.getId());

        assertThat(foundProfile).isPresent();
        assertThat(foundProfile.get().getUser().getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("ServiceAreas 및 Districts 조회")
    void testFindServiceAreasWithDistricts() {
        // GroomerServiceArea 설정
        GroomerServiceArea serviceArea = new GroomerServiceArea(groomerProfile, district);
        entityManager.persist(serviceArea);
        entityManager.flush();
        entityManager.clear();

        List<DistrictResponseDto> districts = groomerProfileRepository.findServiceAreasWithDistricts(groomerProfile.getId());

        assertThat(districts).hasSize(1);
        assertThat(districts.get(0).getDistrict()).isEqualTo("종로구");
        assertThat(districts.get(0).getCity()).isEqualTo("서울시");
    }

    @Test
    @DisplayName("GroomerServices 조회")
    void testFindGroomerServices() {
        // GroomerService 설정
        GroomerService groomerService = new GroomerService("Haircut", false);
        entityManager.persist(groomerService);

        // GroomerCanService 설정
        GroomerCanService canService = new GroomerCanService(groomerProfile, groomerService);
        entityManager.persist(canService);
        entityManager.flush();
        entityManager.clear();

        List<GroomerServicesResponseDto> services = groomerProfileRepository.findGroomerServices(groomerProfile.getId());

        assertThat(services).hasSize(1);
        assertThat(services.get(0).getDescription()).isEqualTo("Haircut");
        assertThat(services.get(0).getIsCustom()).isFalse();
    }

    @Test
    @DisplayName("Badges 조회")
    void testFindBadgesByProfileId() {
        // Badge 설정
        Badge badge = new Badge("Best Groomer", "badge1.png");
        entityManager.persist(badge);

        // GroomerBadge 설정
        GroomerBadge groomerBadge = new GroomerBadge(groomerProfile, badge);

        entityManager.persist(groomerBadge);
        entityManager.flush();
        entityManager.clear();

        List<BadgeResponseDto> badges = groomerProfileRepository.findBadgesByProfileId(groomerProfile.getId());

        assertThat(badges).hasSize(1);
        assertThat(badges.get(0).getName()).isEqualTo("Best Groomer");
        assertThat(badges.get(0).getBadgeImage()).isEqualTo("badge1.png");
    }
}
