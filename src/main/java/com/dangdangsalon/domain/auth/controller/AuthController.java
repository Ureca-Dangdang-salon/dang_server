package com.dangdangsalon.domain.auth.controller;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.auth.dto.JoinAdditionalInfoDto;
import com.dangdangsalon.domain.auth.service.AuthService;
import com.dangdangsalon.util.ApiUtil;
import com.dangdangsalon.util.ApiUtil.ApiSuccess;
import com.dangdangsalon.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ApiSuccess<?> refreshAccessToken(HttpServletRequest request) {

        String refreshToken = cookieUtil.getCookieValue("refreshToken", request);

        Map<String, String> responseBody = authService.refreshAccessToken(refreshToken);

        return ApiUtil.success(responseBody);
    }

    @PostMapping("/join")
    public ApiSuccess<?> completeSignup(@RequestBody JoinAdditionalInfoDto requestDto,
                                        @AuthenticationPrincipal CustomOAuth2User user) {
        authService.completeRegister(user.getUserId(), requestDto);

        return ApiUtil.success("회원가입이 성공적으로 완료되었습니다.");
    }
}
