package com.dangdangsalon.domain.mypage.service;

import com.dangdangsalon.domain.estimate.entity.EstimateStatus;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerDetails;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.groomerprofile.entity.ServiceType;
import com.dangdangsalon.domain.groomerprofile.repository.GroomerProfileRepository;
import com.dangdangsalon.domain.groomerservice.entity.GroomerService;
import com.dangdangsalon.domain.groomerservice.repository.GroomerServiceRepository;
import com.dangdangsalon.domain.mypage.dto.req.GroomerDetailsUpdateRequestDto;
import com.dangdangsalon.domain.mypage.dto.req.GroomerProfileDetailsRequestDto;
import com.dangdangsalon.domain.mypage.dto.req.GroomerProfileRequestDto;
import com.dangdangsalon.domain.mypage.dto.res.*;
import com.dangdangsalon.domain.region.entity.City;
import com.dangdangsalon.domain.region.entity.District;
import com.dangdangsalon.domain.region.repository.DistrictRepository;
import com.dangdangsalon.domain.user.entity.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;

import java.util.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MyPageGroomerServiceTest {

    @InjectMocks
    private MyPageGroomerService myPageGroomerService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroomerProfileRepository groomerProfileRepository;

    @Mock
    private GroomerServiceRepository groomerServiceRepository;

    @Mock
    private DistrictRepository districtRepository;

    @Test
    @DisplayName("미용사 마이페이지 조회 성공 테스트")
    void getGroomerProfilePage_success() {
        // Given
        Long userId = 1L;
        District district = District.builder()
                .name("종로구")
                .city(City.builder()
                        .name("서울시")
                        .build())
                .build();
        User user = User.builder()
                .email("test@test.com")
                .role(Role.ROLE_USER)
                .district(district)
                .build();
        GroomerProfile groomerProfile = GroomerProfile.builder().user(user).build();

        ReflectionTestUtils.setField(groomerProfile, "id", 1L); // Mock으로 ID 설정

        when(groomerProfileRepository.findByUserIdWithDistrict(userId)).thenReturn(Optional.of(groomerProfile));
        when(groomerProfileRepository.findServiceAreasWithDistricts(1L)).thenReturn(List.of());
        when(groomerProfileRepository.findGroomerServiceDescriptions(1L)).thenReturn(List.of());
        when(groomerProfileRepository.findBadgesByProfileId(1L)).thenReturn(List.of());

        // When
        GroomerProfileResponseDto response = myPageGroomerService.getGroomerProfilePage(userId);

        // Then
        assertNotNull(response);
        verify(groomerProfileRepository).findByUserIdWithDistrict(userId);
    }

    @Test
    @DisplayName("미용사 마이페이지 조회 실패 테스트 - 미용사 없음")
    void getGroomerProfilePage_fail() {
        // Given
        Long userId = 1L;
        when(groomerProfileRepository.findByUserIdWithDistrict(userId)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> myPageGroomerService.getGroomerProfilePage(userId));
        assertEquals("해당 미용사를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("미용사 프로필 조회 성공 테스트")
    void testGetGroomerProfile_success() {
        // Given
        Long profileId = 1L;
        GroomerProfile mockProfile = createMockGroomerProfile();
        when(groomerProfileRepository.findById(profileId)).thenReturn(Optional.of(mockProfile));
        when(groomerProfileRepository.findServiceAreasWithDistricts(profileId)).thenReturn(createMockDistrictResponse());
        when(groomerProfileRepository.findGroomerServiceDescriptions(profileId)).thenReturn(createMockServiceResponse());
        when(groomerProfileRepository.findBadgesByProfileId(profileId)).thenReturn(createMockBadgeResponse());

        // When
        GroomerProfileDetailsResponseDto responseDto = myPageGroomerService.getGroomerProfile(profileId);

        // Then
        assertNotNull(responseDto);
        assertEquals(mockProfile.getId(), responseDto.getProfileId());
        verify(groomerProfileRepository).findById(profileId);
    }

    @Test
    @DisplayName("미용사 프로필 조회 실패 테스트 - 권한 없음")
    void testGetGroomerProfile_notFound() {
        // Given
        Long profileId = 1L;
        when(groomerProfileRepository.findById(profileId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> myPageGroomerService.getGroomerProfile(profileId));
    }

    @Test
    @DisplayName("미용사 프로필 저장 성공 테스트")
    void saveGroomerProfile_success() {
        // Given
        Long userId = 1L;
        User user = User.builder().email("test@test.com").role(Role.ROLE_USER).build();
        GroomerProfileRequestDto requestDto = GroomerProfileRequestDto.builder().servicesOfferedId(List.of()).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        myPageGroomerService.saveGroomerProfile(requestDto, userId);

        // Then
        verify(groomerProfileRepository).save(any(GroomerProfile.class));
        assertEquals(Role.ROLE_SALON, user.getRole());
    }

    @Test
    @DisplayName("미용사 프로필 저장 실패 테스트 - 유효하지 않은 서비스 ID 포함")
    void saveGroomerProfile_fail_invalidServiceId() {
        // Given
        Long userId = 1L;
        User user = User.builder().email("test@test.com").role(Role.ROLE_USER).build();
        GroomerProfileRequestDto requestDto = GroomerProfileRequestDto.builder()
                .servicesOfferedId(List.of(1L, 2L))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(groomerServiceRepository.findAllById(requestDto.getServicesOfferedId())).thenReturn(List.of());

        // When & Then
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> myPageGroomerService.saveGroomerProfile(requestDto, userId));
        assertEquals("유효하지 않은 서비스 ID가 포함되어 있습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("미용사 프로필 상세 저장 성공 테스트")
    void testSaveGroomerProfileDetails_success() {
        // Given
        Long userId = 1L;
        GroomerProfileDetailsRequestDto requestDto = createMockDetailRequestDto();
        GroomerProfile mockProfile = createMockGroomerProfile();
        when(groomerProfileRepository.findByUserIdWithDistrict(userId)).thenReturn(Optional.of(mockProfile));

        // When
        myPageGroomerService.saveGroomerProfileDetails(requestDto, userId);

        // Then
        verify(groomerProfileRepository).findByUserIdWithDistrict(userId);
    }

    @Test
    @DisplayName("미용사 프로필 상세 저장 실패 테스트 - 프로필 없음")
    void testSaveGroomerProfileDetails_invalidDistrictIds() {
        /// Given
        Long userId = 1L;
        when(groomerProfileRepository.findByUserIdWithDistrict(userId)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> myPageGroomerService.getGroomerProfilePage(userId));
        assertEquals("해당 미용사를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("미용사 프로필 업데이트 성공 테스트")
    void testUpdateGroomerProfile_success() {
        // Given
        Long userId = 1L;
        Long profileId = 2L;
        User user = User.builder().build();
        GroomerDetailsUpdateRequestDto requestDto = createMockUpdateRequestDto();
        GroomerProfile mockProfile = createMockGroomerProfile(user);
        ReflectionTestUtils.setField(user, "id", 1L);

        when(groomerProfileRepository.findById(profileId)).thenReturn(Optional.of(mockProfile));
        when(groomerServiceRepository.findAllById(requestDto.getServicesOfferedId())).thenReturn(createMockServices());
        when(districtRepository.findAllById(requestDto.getServicesDistrictIds())).thenReturn(createMockDistricts());

        // When
        myPageGroomerService.updateGroomerProfile(requestDto, userId, profileId);

        // Then
        verify(groomerProfileRepository).findById(profileId);
        verify(groomerServiceRepository).findAllById(requestDto.getServicesOfferedId());
        verify(districtRepository).findAllById(requestDto.getServicesDistrictIds());
    }

    @Test
    @DisplayName("미용사 프로필 업데이트 - 실패 (권한 없음)")
    void testUpdateGroomerProfile_invalidUser() {
        // Given
        Long userId = 1L;
        Long profileId = 2L;
        User user = User.builder().build();

        GroomerDetailsUpdateRequestDto requestDto = createMockUpdateRequestDto();
        GroomerProfile mockProfile = createMockGroomerProfile(user);

        ReflectionTestUtils.setField(user, "id", 2L);

        when(groomerProfileRepository.findById(profileId)).thenReturn(Optional.of(mockProfile));

        // When & Then
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> myPageGroomerService.updateGroomerProfile(requestDto, userId, profileId));
        assertEquals("프로필을 수정할 권한이 없습니다. userId : 1", exception.getMessage());
    }

    @Test
    @DisplayName("미용사 프로필 삭제 성공 테스트")
    void deleteGroomerProfile_success() {
        // Given
        Long userId = 1L;
        User user = User.builder().build();

        GroomerProfile groomerProfile = GroomerProfile.builder().user(user).build();
        ReflectionTestUtils.setField(groomerProfile, "id", 1L);
        ReflectionTestUtils.setField(user, "id", 1L);

        when(groomerProfileRepository.findById(1L)).thenReturn(Optional.of(groomerProfile));

        // When
        myPageGroomerService.deleteGroomerProfile(userId, 1L);

        // Then
        verify(groomerProfileRepository).delete(groomerProfile);
    }

    @Test
    @DisplayName("미용사 프로필 삭제 실패 테스트 - 권한 없음")
    void deleteGroomerProfile_fail_noPermission() {
        // Given
        Long userId = 1L;
        User user = User.builder().build();

        GroomerProfile groomerProfile = GroomerProfile.builder().user(user).build();
        ReflectionTestUtils.setField(groomerProfile, "id", 1L);
        ReflectionTestUtils.setField(user, "id", 2L);

        when(groomerProfileRepository.findById(1L)).thenReturn(Optional.of(groomerProfile));

        // When & Then
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> myPageGroomerService.deleteGroomerProfile(userId, 1L));
        assertEquals("프로필을 삭제할 권한이 없습니다. userId : 1", exception.getMessage());
    }

    @Test
    @DisplayName("getGroomerProfileMainPage - 성공적으로 상위 5명 지역/전국 반환")
    void testGetGroomerProfileMainPage_Success() {
        // Given

        City city = City.builder().name("Seoul").build();
        District district = District.builder().city(city).name("Gangnam").build();
        User user = User.builder().district(district).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Pageable pageable = PageRequest.of(0, 5);

        // Mock 지역 top 5
        List<GroomerRecommendResponseDto> districtTop = List.of(
                new GroomerRecommendResponseDto(1L, "LocalGroomer1", "img1", "Seoul", "Gangnam"),
                new GroomerRecommendResponseDto(2L, "LocalGroomer2", "img2", "Seoul", "Gangnam")
        );

        // Mock 전국 top 5
        List<GroomerRecommendResponseDto> nationalTop = List.of(
                new GroomerRecommendResponseDto(10L, "NationalGroomer1", "nimg1", "Busan", "Haeundae"),
                new GroomerRecommendResponseDto(11L, "NationalGroomer2", "nimg2", "Daegu", "Suseong")
        );

        when(groomerProfileRepository.findTop5ByAcceptedOrdersWithDto(EstimateStatus.ACCEPTED, pageable)).thenReturn(nationalTop);
        when(groomerProfileRepository.findTop5GroomersInArea("Gangnam", pageable)).thenReturn(districtTop);

        // When
        GroomerMainResponseDto result = myPageGroomerService.getGroomerProfileMainPage(1L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getDistrictTopGroomers().size());
        assertEquals(2, result.getNationalTopGroomers().size());

        verify(userRepository, times(1)).findById(1L);
        verify(groomerProfileRepository, times(1)).findTop5ByAcceptedOrdersWithDto(EstimateStatus.ACCEPTED, pageable);
        verify(groomerProfileRepository, times(1)).findTop5GroomersInArea("Gangnam", pageable);
    }

    @Test
    @DisplayName("getGroomerProfileMainPage - 결과 비어있어도 정상처리")
    void testGetGroomerProfileMainPage_EmptyResults() {
        City city = City.builder().name("Seoul").build();
        District district = District.builder().city(city).name("Gangnam").build();
        User user = User.builder().district(district).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(groomerProfileRepository.findTop5ByAcceptedOrdersWithDto(EstimateStatus.ACCEPTED, PageRequest.of(0, 5))).thenReturn(List.of());
        when(groomerProfileRepository.findTop5GroomersInArea("Gangnam", PageRequest.of(0, 5))).thenReturn(List.of());

        GroomerMainResponseDto result = myPageGroomerService.getGroomerProfileMainPage(1L);

        assertNotNull(result);
        assertTrue(result.getDistrictTopGroomers().isEmpty());
        assertTrue(result.getNationalTopGroomers().isEmpty());
    }

    private GroomerProfile createMockGroomerProfile() {
        return GroomerProfile.builder()
                .name("Test Groomer")
                .contactHours("9AM - 5PM")
                .serviceType(ServiceType.ANY)
                .details(new GroomerDetails())
                .build();
    }

    private GroomerProfile createMockGroomerProfile(User user) {
        return GroomerProfile.builder()
                .name("Test Groomer")
                .contactHours("9AM - 5PM")
                .serviceType(ServiceType.ANY)
                .details(new GroomerDetails())
                .user(user)
                .build();
    }

    private GroomerProfileRequestDto createMockRequestDto() {
        return GroomerProfileRequestDto.builder()
                .name("미용사")
                .phone("phone")
                .contactHours("10-18")
                .serviceType(ServiceType.SHOP)
                .servicesDistrictIds(List.of(1L, 2L))
                .servicesOfferedId(List.of(1L, 2L))
                .build();
    }

    private GroomerProfileDetailsRequestDto createMockDetailRequestDto() {
        return GroomerProfileDetailsRequestDto.builder()
                .imageKey("image")
                .businessNumber("number")
                .address("address")
                .experience("experience")
                .description("description")
                .startMessage("startMessage")
                .faq("faq")
                .certifications(List.of("자격증1", "자격증1"))
                .build();
    }

    private GroomerDetailsUpdateRequestDto createMockUpdateRequestDto() {
        return GroomerDetailsUpdateRequestDto.builder()
                .name("Updated Groomer")
                .contactHours("10AM - 6PM")
                .serviceType(ServiceType.VISIT)
                .phone("123-456-7890")
                .servicesDistrictIds(List.of(1L, 2L))
                .servicesOfferedId(List.of(3L, 4L))
                .certifications(List.of("Certification3", "Certification4"))
                .build();
    }

    private List<DistrictResponseDto> createMockDistrictResponse() {
        return List.of(new DistrictResponseDto(1L, "District1", "city1"), new DistrictResponseDto(2L, "District2", "city2"));
    }

    private List<String> createMockServiceResponse() {
        return List.of("Service1", "Service2");
    }

    private List<BadgeResponseDto> createMockBadgeResponse() {
        return List.of(new BadgeResponseDto(1L, "Badge1", "Badge1.jpg"), new BadgeResponseDto(2L, "Badge2", "Badge2.jpg"));
    }

    private List<District> createMockDistricts() {
        return List.of(new District("District1", new City("city1")), new District("District2", new City("city1")));
    }

    private List<GroomerService> createMockServices() {
        return List.of(new GroomerService("Service1", false), new GroomerService("Service2", false));
    }

}
