package com.dangdangsalon.domain.mypage.controller;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.mypage.dto.req.GroomerDetailsUpdateRequestDto;
import com.dangdangsalon.domain.mypage.dto.req.GroomerProfileDetailsRequestDto;
import com.dangdangsalon.domain.mypage.dto.req.GroomerProfileRequestDto;
import com.dangdangsalon.domain.mypage.dto.res.GroomerMainResponseDto;
import com.dangdangsalon.domain.mypage.dto.res.GroomerProfileDetailsResponseDto;
import com.dangdangsalon.domain.mypage.dto.res.GroomerProfileResponseDto;
import com.dangdangsalon.domain.mypage.service.MyPageGroomerService;
import com.dangdangsalon.util.ApiUtil;
import com.dangdangsalon.util.ApiUtil.ApiSuccess;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groomerprofile")
@RequiredArgsConstructor
public class MyPageGroomerController {

    private final MyPageGroomerService myPageGroomerService;

    @GetMapping("")
    public ApiSuccess<?> getGroomerProfile(@AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();
        GroomerProfileResponseDto groomerProfile = myPageGroomerService.getGroomerProfilePage(userId);
        return ApiUtil.success(groomerProfile);
    }

    @GetMapping("/{groomerProfileId}")
    public ApiSuccess<?> getGroomerProfile(@PathVariable Long groomerProfileId) {
        GroomerProfileDetailsResponseDto groomerProfile = myPageGroomerService.getGroomerProfile(groomerProfileId);
        return ApiUtil.success(groomerProfile);
    }

    @PostMapping("")
    public ApiSuccess<?> saveGroomerProfile(@RequestBody GroomerProfileRequestDto requestDto,
                                           @AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();
        myPageGroomerService.saveGroomerProfile(requestDto, userId);
        return ApiUtil.success("미용사 프로필 등록이 완료되었습니다.");
    }

    @PostMapping("/detail")
    public ApiSuccess<?> saveGroomerProfile(@RequestBody GroomerProfileDetailsRequestDto requestDto,
                                            @AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();
        myPageGroomerService.saveGroomerProfileDetails(requestDto, userId);
        return ApiUtil.success("미용사 프로필 상세 정보 등록이 완료되었습니다.");
    }

    @PutMapping("/{profileId}")
    public ApiSuccess<?> saveGroomerProfileDetail(@RequestBody GroomerDetailsUpdateRequestDto requestDto,
                                                  @AuthenticationPrincipal CustomOAuth2User user,
                                                  @PathVariable Long profileId) {
        Long userId = user.getUserId();
        myPageGroomerService.updateGroomerProfile(requestDto, userId, profileId);
        return ApiUtil.success("미용사 프로필 정보 수정이 완료되었습니다.");
    }

    @DeleteMapping("/{profileId}")
    public ApiSuccess<?> deleteGroomerProfile(@AuthenticationPrincipal CustomOAuth2User user,
                                          @PathVariable Long profileId) {
        Long userId = user.getUserId();
        myPageGroomerService.deleteGroomerProfile(userId, profileId);
        return ApiUtil.success("미용사 프로필 삭제가 완료되었습니다.");
    }

    @GetMapping("/main")
    public ApiSuccess<?> getGroomerProfileMainPage(@AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();
        GroomerMainResponseDto groomerProfile = myPageGroomerService.getGroomerProfileMainPage(userId);
        return ApiUtil.success(groomerProfile);
    }

    @GetMapping("/check/{name}")
    public ApiSuccess<?> getCheckName(@PathVariable String name) {
        boolean nameDuplicate = myPageGroomerService.isNameDuplicate(name);
        return ApiUtil.success(nameDuplicate);
    }
}
