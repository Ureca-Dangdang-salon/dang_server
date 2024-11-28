package com.dangdangsalon.domain.mypage.controller;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.mypage.dto.req.DogProfileRequestDto;
import com.dangdangsalon.domain.mypage.dto.res.MyDogProfileResponseDto;
import com.dangdangsalon.domain.mypage.dto.res.UserProfileResponseDto;
import com.dangdangsalon.domain.mypage.service.MyPageDogProfileService;
import com.dangdangsalon.util.ApiUtil;
import com.dangdangsalon.util.ApiUtil.ApiSuccess;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dogprofile")
@RequiredArgsConstructor
public class MyPageDogProfileController {

    private final MyPageDogProfileService myPageDogProfileService;

    @GetMapping("")
    public ApiSuccess<?> getUserProfile(@AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();
        UserProfileResponseDto userProfile = myPageDogProfileService.getUserProfile(userId);
        return ApiUtil.success(userProfile);
    }

    @GetMapping("/{dogProfileId}")
    public ApiSuccess<?> getDogProfile(@AuthenticationPrincipal CustomOAuth2User user,
                                       @PathVariable Long dogProfileId) {
        Long userId = user.getUserId();
        MyDogProfileResponseDto dogProfile = myPageDogProfileService.getDogProfile(userId, dogProfileId);
        return ApiUtil.success(dogProfile);
    }

    @PostMapping("")
    public ApiSuccess<?> saveDogProfile(@RequestBody DogProfileRequestDto requestDto,
            @AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();
        myPageDogProfileService.saveDogProfile(requestDto, userId);
        return ApiUtil.success("반려견 프로필 등록이 완료되었습니다.");
    }

    @PutMapping("/{dogProfileId}")
    public ApiSuccess<?> updateDogProfile(@RequestBody DogProfileRequestDto requestDto,
                                        @AuthenticationPrincipal CustomOAuth2User user,
                                        @PathVariable Long dogProfileId) {
        Long userId = user.getUserId();
        myPageDogProfileService.updateDogProfile(requestDto, userId, dogProfileId);
        return ApiUtil.success("반려견 프로필 수정이 완료되었습니다.");
    }

    @DeleteMapping("/{dogProfileId}")
    public ApiSuccess<?> deleteDogProfile(@AuthenticationPrincipal CustomOAuth2User user,
                                          @PathVariable Long dogProfileId) {
        Long userId = user.getUserId();
        myPageDogProfileService.deleteDogProfile(userId, dogProfileId);
        return ApiUtil.success("반려견 프로필 삭제가 완료되었습니다.");
    }

}
