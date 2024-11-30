package com.dangdangsalon.domain.auth.controller;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.auth.dto.JoinAdditionalInfoDto;
import com.dangdangsalon.domain.auth.service.AuthService;
import com.dangdangsalon.util.ApiUtil;
import com.dangdangsalon.util.ApiUtil.ApiSuccess;
import com.dangdangsalon.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final CookieUtil cookieUtil;
    private final AuthService authService;

    @PostMapping("/refresh")
    public ApiSuccess<?> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = cookieUtil.getCookieValue("refreshToken", request);

        authService.refreshAccessToken(refreshToken, response);

        return ApiUtil.success("액세스 토큰 갱신에 성공했습니다.");
    }

    @PostMapping("/join")
    public ApiSuccess<?> completeSignup(@RequestBody JoinAdditionalInfoDto requestDto,
                                        @AuthenticationPrincipal CustomOAuth2User user) {
        authService.completeRegister(user.getUserId(), requestDto);

        return ApiUtil.success("회원가입에 성공했습니다.");
    }

    @DeleteMapping("/delete")
    public ApiSuccess<?> deleteUser(@AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();

        authService.deleteUser(userId);

        return ApiUtil.success("회원탈퇴에 성공했습니다.");
    }

    @PostMapping("/logout")
    public ApiSuccess<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtil.getCookieValue("Refresh-Token", request);

        authService.logout(refreshToken, response);

        return ApiUtil.success("로그아웃이 완료되었습니다.");
    }
}
