package com.dangdangsalon.domain.dogprofile.service;

import com.dangdangsalon.domain.dogprofile.dto.DogProfileResponseDto;
import com.dangdangsalon.domain.dogprofile.entity.DogProfile;
import com.dangdangsalon.domain.dogprofile.repository.DogProfileRepository;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DogProfileService {

    private final DogProfileRepository dogProfileRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<DogProfileResponseDto> getDogProfilesByUserId(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 아이디를 찾을 수 없습니다 : " + userId));

        List<DogProfile> dogProfiles = dogProfileRepository.findByUser(user)
                .orElse(Collections.emptyList());

        return dogProfiles.stream()
                .map(dog -> new DogProfileResponseDto(dog.getImageKey(), dog.getName()))
                .toList();
    }
}