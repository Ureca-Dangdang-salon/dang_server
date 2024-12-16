package com.dangdangsalon.domain.mypage.service;

import com.dangdangsalon.domain.mypage.dto.req.CommonProfileRequestDto;
import com.dangdangsalon.domain.mypage.dto.res.CommonProfileResponseDto;
import com.dangdangsalon.domain.region.entity.City;
import com.dangdangsalon.domain.region.entity.District;
import com.dangdangsalon.domain.region.repository.DistrictRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
@ExtendWith(MockitoExtension.class)
class MyPageCommonServiceTest {

    @InjectMocks
    private MyPageCommonService myPageCommonService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DistrictRepository districtRepository;

    @Test
    @DisplayName("getUserinfo - 유저 정보 정상 조회")
    void getUserinfoSuccess() {
        // given
        Long userId = 1L;

        User mockUser = mock(User.class);
        District mockDistrict = mock(District.class);
        City mockCity = mock(City.class);

        when(mockUser.getDistrict()).thenReturn(mockDistrict);
        when(mockDistrict.getCity()).thenReturn(mockCity);
        when(mockDistrict.getName()).thenReturn("종로구");
        when(mockCity.getName()).thenReturn("서울시");

        when(mockUser.getImageKey()).thenReturn("imageKey123");
        when(mockUser.getEmail()).thenReturn("user@example.com");


        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // when
        CommonProfileResponseDto responseDto = myPageCommonService.getUserinfo(userId);

        // then
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getImageKey()).isEqualTo("imageKey123");
        assertThat(responseDto.getEmail()).isEqualTo("user@example.com");
        assertThat(responseDto.getDistrict()).isEqualTo("종로구");
        assertThat(responseDto.getCity()).isEqualTo("서울시");
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("getUserinfo - 유저 ID가 존재하지 않을 때 예외 발생")
    void getUserinfoNotFound() {
        // given
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> myPageCommonService.getUserinfo(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유저 아이디를 찾을 수 없습니다. userId : " + userId);

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("updateUserinfo - 유저 정보 업데이트 성공")
    void updateUserinfoSuccess() {
        // given
        Long userId = 1L;
        Long districtId = 101L;

        User mockUser = mock(User.class);
        District mockDistrict = mock(District.class);

        CommonProfileRequestDto requestDto = new CommonProfileRequestDto("newImageKey123", "new@example.com", districtId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(districtRepository.findById(districtId)).thenReturn(Optional.of(mockDistrict));

        // when
        myPageCommonService.updateUserinfo(userId, requestDto);

        // then
        verify(userRepository, times(1)).findById(userId);
        verify(districtRepository, times(1)).findById(districtId);
        verify(mockUser, times(1)).updateUserInfo(requestDto.getProfileImage(),
                requestDto.getEmail(), mockDistrict);
    }

    @Test
    @DisplayName("updateUserinfo - 유저 ID가 존재하지 않을 때 예외 발생")
    void updateUserinfoUserNotFound() {
        // given
        Long userId = 99L;
        CommonProfileRequestDto requestDto = new CommonProfileRequestDto("imageKey",
                "email@example.com", 101L);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> myPageCommonService.updateUserinfo(userId, requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("유저 아이디를 찾을 수 없습니다. userId : " + userId);

        verify(userRepository, times(1)).findById(userId);
        verify(districtRepository, times(0)).findById(anyLong());
    }

    @Test
    @DisplayName("updateUserinfo - 지역 ID가 존재하지 않을 때 예외 발생")
    void updateUserinfoDistrictNotFound() {
        // given
        Long userId = 1L;
        Long districtId = 101L;

        User mockUser = mock(User.class);
        CommonProfileRequestDto requestDto = new CommonProfileRequestDto("imageKey",
                "email@example.com", districtId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(districtRepository.findById(districtId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> myPageCommonService.updateUserinfo(userId, requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("지역을 찾을 수 없습니다. districtId : " + districtId);

        verify(userRepository, times(1)).findById(userId);
        verify(districtRepository, times(1)).findById(districtId);
    }
}