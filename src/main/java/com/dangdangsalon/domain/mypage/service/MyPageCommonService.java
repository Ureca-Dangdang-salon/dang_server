package com.dangdangsalon.domain.mypage.service;

import com.dangdangsalon.domain.mypage.dto.req.CommonProfileRequestDto;
import com.dangdangsalon.domain.mypage.dto.res.CommonProfileResponseDto;
import com.dangdangsalon.domain.region.entity.District;
import com.dangdangsalon.domain.region.repository.DistrictRepository;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class MyPageCommonService {
    private final UserRepository userRepository;
    private final DistrictRepository districtRepository;

    @Transactional(readOnly = true)
    public CommonProfileResponseDto getUserinfo(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new IllegalArgumentException("유저 아이디를 찾을 수 없습니다. userId : " + userId));

        return CommonProfileResponseDto.createCommonProfileResponseDto(user);
    }

    @Transactional
    public void updateUserinfo(Long userId, CommonProfileRequestDto requestDto) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new IllegalArgumentException("유저 아이디를 찾을 수 없습니다. userId : " + userId));

        District district = districtRepository.findById(requestDto.getDistrictId()).orElseThrow(() ->
                new IllegalArgumentException("지역을 찾을 수 없습니다. districtId : " + requestDto.getDistrictId()));;

        user.updateUserInfo(requestDto.getImageKey(), requestDto.getEmail(), district);
    }
}