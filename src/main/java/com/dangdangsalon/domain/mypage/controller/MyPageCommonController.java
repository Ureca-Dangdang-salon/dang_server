package com.dangdangsalon.domain.mypage.controller;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.mypage.dto.req.CommonProfileRequestDto;
import com.dangdangsalon.domain.mypage.dto.res.CommonProfileResponseDto;
import com.dangdangsalon.domain.mypage.service.MyPageCommonService;
import com.dangdangsalon.util.ApiUtil;
import com.dangdangsalon.util.ApiUtil.ApiSuccess;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class MyPageCommonController {

    private final MyPageCommonService myPageCommonService;

    @GetMapping("")
    public ApiSuccess<?> getUserProfile(@AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();
        CommonProfileResponseDto userinfo = myPageCommonService.getUserinfo(userId);
        return ApiUtil.success(userinfo);
    }

    @PutMapping("")
    public ApiSuccess<?> updateUserProfile(
            @AuthenticationPrincipal CustomOAuth2User user,
            @RequestBody CommonProfileRequestDto requestDto) {
        Long userId = user.getUserId();
        myPageCommonService.updateUserinfo(userId, requestDto);

        return ApiUtil.success("유저 정보가 변경되었습니다.");
    }

}
