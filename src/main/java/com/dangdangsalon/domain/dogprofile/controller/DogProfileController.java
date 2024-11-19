package com.dangdangsalon.domain.dogprofile.controller;

import com.dangdangsalon.domain.dogprofile.dto.DogProfileResponseDto;
import com.dangdangsalon.domain.dogprofile.service.DogProfileService;
import com.dangdangsalon.util.ApiUtil;
import lombok.RequiredArgsConstructor;
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

    /**
     *  userId 토큰에서 가져오는 걸로 추후 변경
     */
    @GetMapping("/estimaterequest/dogprofiles")
    public ApiUtil.ApiSuccess<?> getEstimateRequestDogProfiles(@RequestParam Long userId) {
        List<DogProfileResponseDto> dogProfiles = dogProfileService.getDogProfilesByUserId(userId);
        return ApiUtil.success(dogProfiles);
    }
}