package com.dangdangsalon.domain.mypage.service;

import com.dangdangsalon.domain.dogprofile.entity.*;
import com.dangdangsalon.domain.dogprofile.feature.entity.Feature;
import com.dangdangsalon.domain.dogprofile.repository.DogProfileRepository;
import com.dangdangsalon.domain.dogprofile.repository.FeatureRepository;
import com.dangdangsalon.domain.mypage.dto.req.DogProfileRequestDto;
import com.dangdangsalon.domain.mypage.dto.res.MyDogProfileResponseDto;
import com.dangdangsalon.domain.mypage.dto.res.UserProfileResponseDto;
import com.dangdangsalon.domain.region.entity.City;
import com.dangdangsalon.domain.region.entity.District;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;

import java.util.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MyPageDogProfileServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private DogProfileRepository dogProfileRepository;

    @Mock
    private FeatureRepository featureRepository;

    @InjectMocks
    private MyPageDogProfileService myPageDogProfileService;

    private User mockUser;
    private DogProfile mockDogProfile;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .name("유저")
                .build();

        mockDogProfile = DogProfile.builder()
                .name("반려견")
                .user(mockUser)
                .build();
    }

    @Test
    @DisplayName("유저 프로필 조회 성공 테스트")
    void testGetUserProfile_Success() {
        // given
        // mock 객체 생성
        User mockUser = mock(User.class);

        // mockCity 객체 생성
        City mockCity = mock(City.class);
        when(mockCity.getName()).thenReturn("서울시");  // City 객체에서 getName()이 "서울시"를 반환하도록 설정

        // mockDistrict 객체 생성
        District mockDistrict = mock(District.class);
        when(mockDistrict.getCity()).thenReturn(mockCity);  // District 객체에서 getCity()가 mockCity를 반환하도록 설정
        when(mockDistrict.getName()).thenReturn("종로구");

        // mockUser 객체가 getDistrict()를 호출할 때 mockDistrict를 반환하도록 설정
        when(mockUser.getDistrict()).thenReturn(mockDistrict);
        when(mockUser.getName()).thenReturn("유저");

        when(mockUser.getCoupons()).thenReturn(List.of()); // 예시로 빈 리스트
        when(mockUser.getOrders()).thenReturn(List.of()); // 예시로 빈 리스트

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // when
        UserProfileResponseDto result = myPageDogProfileService.getUserProfile(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("유저");
        // 여기서 result.getCityName()이 "서울시"인지 확인할 수 있습니다.
        assertThat(result.getCity()).isEqualTo("서울시");
    }

    @Test
    @DisplayName("유저 프로필 조회 실패 테스트 - 유저 없음")
    void testGetUserProfile_Failure_UserNotFound() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when / then
        assertThrows(IllegalArgumentException.class, () -> {
            myPageDogProfileService.getUserProfile(1L);
        });
    }

    @Test
    @DisplayName("반려견 프로필 조회 성공 테스트")
    void testGetDogProfile_Success() {
        // given
        // mock 객체로 DogAge 생성
        DogAge mockDogAge = mock(DogAge.class);
        when(mockDogAge.getYear()).thenReturn(5); // 나이를 5로 설정

        // mockDogProfile 객체를 mock으로 설정
        DogProfile mockDogProfile = mock(DogProfile.class);
        when(mockDogProfile.getAge()).thenReturn(mockDogAge);
        when(mockDogProfile.getName()).thenReturn("반려견");

        when(dogProfileRepository.findByIdAndUserIdWithFeatures(1L, 1L)).thenReturn(Optional.of(mockDogProfile));

        // when
        MyDogProfileResponseDto result = myPageDogProfileService.getDogProfile(1L, 1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("반려견");
    }

    @Test
    @DisplayName("반려견 프로필 조회 실패 테스트 - 반려견 없음")
    void testGetDogProfile_Failure_DogProfileNotFound() {
        // given
        when(dogProfileRepository.findByIdAndUserIdWithFeatures(1L, 1L)).thenReturn(Optional.empty());

        // when / then
        assertThrows(IllegalArgumentException.class, () -> {
            myPageDogProfileService.getDogProfile(1L, 1L);
        });
    }

    @Test
    @DisplayName("반려견 프로필 저장 성공 테스트")
    void testSaveDogProfile_Success() {
        // given
        DogProfileRequestDto request = new DogProfileRequestDto("수정된 반려견", "수정된 이미지",
                "견종", 2, 3, Gender.MALE, Neutering.N,
                15, List.of(1L),"추가 특징");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(featureRepository.findAllById(anyList())).thenReturn(List.of(new Feature("특징", false)));

        // when
        myPageDogProfileService.saveDogProfile(request, 1L);

        // then
        verify(dogProfileRepository, times(1)).save(any(DogProfile.class));
    }

    @Test
    @DisplayName("반려견 프로필 저장 실패 테스트 - 유저 없음")
    void testSaveDogProfile_Failure_UserNotFound() {
        // given
        DogProfileRequestDto request = new DogProfileRequestDto("수정된 반려견", "수정된 이미지",
                "견종", 2, 3, Gender.MALE, Neutering.N,
                15, List.of(1L),"추가 특징");

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when / then
        assertThrows(IllegalArgumentException.class, () -> {
            myPageDogProfileService.saveDogProfile(request, 1L);
        });
    }

    @Test
    @DisplayName("반려견 프로필 수정 성공 테스트")
    void testUpdateDogProfile_Success() {
        // given
        DogProfileRequestDto request = new DogProfileRequestDto("수정된 반려견", "수정된 이미지",
                "말티즈", 2, 3, Gender.MALE, Neutering.N,
                15, List.of(1L), "추가 특징");

        // mockUser를 mock 객체로 생성하고 id 설정
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(1L);  // mockUser의 getId()가 1L을 반환하도록 설정

        // 실제 DogProfile 객체를 생성
        DogProfile dogProfile = DogProfile.builder()
                .imageKey("이미지키")
                .name("반려견 이름")
                .species("견종")
                .age(DogAge.createDogAge(3, 5))
                .gender(Gender.MALE)
                .neutering(Neutering.Y)
                .weight(10)
                .user(mockUser)
                .build();

        // dogProfileRepository에서 실제 dogProfile을 반환하도록 설정
        when(dogProfileRepository.findById(1L)).thenReturn(Optional.of(dogProfile));
        when(featureRepository.findAllById(anyList())).thenReturn(List.of(new Feature("특징", false)));

        // when
        myPageDogProfileService.updateDogProfile(request, 1L, 1L);

        // then
        assertThat(dogProfile.getName()).isEqualTo("수정된 반려견");
        System.out.println(dogProfile.getName());
        assertThat(dogProfile.getImageKey()).isEqualTo("수정된 이미지");
        System.out.println(dogProfile.getImageKey());
        assertThat(dogProfile.getSpecies()).isEqualTo("말티즈");
        System.out.println(dogProfile.getSpecies());
    }

    @Test
    @DisplayName("반려견 프로필 수정 실패 테스트 - 반려견 없음")
    void testUpdateDogProfile_Failure_DogProfileNotFound() {
        // given
        DogProfileRequestDto request = new DogProfileRequestDto("수정된 반려견", "수정된 이미지",
                "견종", 2, 3, Gender.MALE, Neutering.N,
                15, List.of(1L),"추가 특징");
        when(dogProfileRepository.findById(1L)).thenReturn(Optional.empty());

        // when / then
        assertThrows(IllegalArgumentException.class, () -> {
            myPageDogProfileService.updateDogProfile(request, 1L, 1L);
        });
    }

    @Test
    @DisplayName("반려견 프로필 삭제 성공 테스트")
    void testDeleteDogProfile_Success() {
        // given
        Long userId = 1L;
        Long dogProfileId = 1L;

        // mockUser 설정
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(userId);

        // 실제 DogProfile 설정
        DogProfile dogProfile = DogProfile.builder()
                .name("반려견 이름")
                .user(mockUser)
                .build();

        ReflectionTestUtils.setField(dogProfile, "id", 1L); // Mock으로 ID 설정

        // dogProfileRepository에서 실제 dogProfile을 반환하도록 설정
        when(dogProfileRepository.findById(dogProfileId)).thenReturn(Optional.of(dogProfile));

        // when
        myPageDogProfileService.deleteDogProfile(userId, dogProfileId);

        // then
        verify(dogProfileRepository, times(1)).delete(dogProfile);  // delete 메서드 호출 확인
    }

    @Test
    @DisplayName("반려견 프로필 삭제 실패 테스트 - 프로필 없음")
    void testDeleteDogProfile_Failure_DogProfileNotFound() {
        // given
        Long userId = 1L;
        Long dogProfileId = 1L;

        // dogProfileRepository에서 반려견 프로필을 찾을 수 없도록 설정
        when(dogProfileRepository.findById(dogProfileId)).thenReturn(Optional.empty());

        // when / then
        assertThrows(IllegalArgumentException.class, () -> {
            myPageDogProfileService.deleteDogProfile(userId, dogProfileId);
        });
    }

    @Test
    @DisplayName("반려견 프로필 삭제 실패 테스트 - 권한 없음")
    void testDeleteDogProfile_Failure_NoPermission() {
        // given
        Long userId = 1L;
        Long dogProfileId = 1L;

        // mockUser 설정
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(2L);

        // 실제 DogProfile 설정
        DogProfile dogProfile = DogProfile.builder()
                .name("반려견 이름")
                .user(mockUser)
                .build();
        ReflectionTestUtils.setField(dogProfile, "id", 1L); // Mock으로 ID 설정

        // dogProfileRepository에서 실제 dogProfile을 반환하도록 설정
        when(dogProfileRepository.findById(dogProfileId)).thenReturn(Optional.of(dogProfile));

        // when / then
        assertThrows(IllegalArgumentException.class, () -> {
            myPageDogProfileService.deleteDogProfile(userId, dogProfileId);
        });
    }

}