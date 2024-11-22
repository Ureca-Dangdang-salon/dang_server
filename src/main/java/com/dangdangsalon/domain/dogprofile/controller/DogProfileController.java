package com.dangdangsalon.domain.dogprofile.controller;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.dogprofile.dto.DogProfileResponseDto;
import com.dangdangsalon.domain.dogprofile.service.DogProfileService;
import com.dangdangsalon.util.ApiUtil;
import com.dangdangsalon.util.ApiUtil.ApiSuccess;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DogProfileController {

    private final DogProfileService dogProfileService;

    @GetMapping("/estimaterequest/dogprofiles")
    public ApiSuccess<?> getEstimateRequestDogProfiles(@AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();
        List<DogProfileResponseDto> dogProfiles = dogProfileService.getDogProfilesByUserId(userId);
        return ApiUtil.success(dogProfiles);
    }
}