package com.dangdangsalon.domain.dogprofile.service;

import com.dangdangsalon.domain.dogprofile.dto.DogProfileResponseDto;
import com.dangdangsalon.domain.dogprofile.entity.DogProfile;
import com.dangdangsalon.domain.dogprofile.repository.DogProfileRepository;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DogProfileServiceTest {

    @InjectMocks
    private DogProfileService dogProfileService;

    @Mock
    private DogProfileRepository dogProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("유저 ID로 강아지 프로필 목록을 조회한다")
    void getDogProfilesByUserId() {
        // given
        Long userId = 1L;
        User user = createUser();
        List<DogProfile> dogProfiles = Arrays.asList(
                createDogProfile("image1.jpg", "구름이", user),
                createDogProfile("image2.jpg", "하늘이", user)
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(dogProfileRepository.findByUser(user)).willReturn(Optional.of(dogProfiles));

        // when
        List<DogProfileResponseDto> result = dogProfileService.getDogProfilesByUserId(userId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getProfileImage()).isEqualTo("image1.jpg");
        assertThat(result.get(0).getName()).isEqualTo("구름이");
        assertThat(result.get(1).getProfileImage()).isEqualTo("image2.jpg");
        assertThat(result.get(1).getName()).isEqualTo("하늘이");

        verify(userRepository).findById(userId);
        verify(dogProfileRepository).findByUser(user);
    }

    @Test
    @DisplayName("존재하지 않는 유저 ID로 조회시 예외가 발생한다")
    void getDogProfilesByUserId_UserNotFound() {
        // given
        Long userId = 999L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> dogProfileService.getDogProfilesByUserId(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유저 아이디를 찾을 수 없습니다 : " + userId);

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("유저의 강아지 프로필이 없는 경우 빈 리스트를 반환한다")
    void getDogProfilesByUserId_EmptyDogProfiles() {
        // given
        Long userId = 1L;
        User user = createUser();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(dogProfileRepository.findByUser(user)).willReturn(Optional.of(Collections.emptyList()));

        // when
        List<DogProfileResponseDto> result = dogProfileService.getDogProfilesByUserId(userId);

        // then
        assertThat(result).isEmpty();

        verify(userRepository).findById(userId);
        verify(dogProfileRepository).findByUser(user);
    }

    private User createUser() {
        return User.builder()
                .email("test1@example.com")
                .name("이민수")
                .build();
    }

    private DogProfile createDogProfile(String imageKey, String name, User user) {
        return DogProfile.builder()
                .imageKey(imageKey)
                .name(name)
                .user(user)
                .build();
    }
}