package com.dangdangsalon.domain.auth.service;

import com.dangdangsalon.domain.auth.dto.CheckLoginDto;
import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.auth.dto.JoinAdditionalInfoDto;
import com.dangdangsalon.domain.region.repository.DistrictRepository;
import com.dangdangsalon.domain.user.entity.Role;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import com.dangdangsalon.exception.TokenExpiredException;
import com.dangdangsalon.util.CookieUtil;
import com.dangdangsalon.util.JwtUtil;
import com.dangdangsalon.util.RedisUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final CookieUtil cookieUtil;

    private final UserRepository userRepository;
    private final DistrictRepository districtRepository;

    public void refreshAccessToken(String refreshToken, HttpServletResponse response) {
        if (jwtUtil.isExpired(refreshToken)) {
            throw new TokenExpiredException();
        }

        Long userId = jwtUtil.getUserId(refreshToken);

        Object cachedRefreshToken = redisUtil.getRefreshToken(userId.toString());
        if (cachedRefreshToken == null || !cachedRefreshToken.equals(refreshToken)) {
            throw new TokenExpiredException();
        }

        String username = jwtUtil.getUsername(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        String newAccessToken = jwtUtil.createAccessToken(userId, username, role);

        Cookie accessTokenCookie = new Cookie("Authorization", null);
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setHttpOnly(true);

        response.addCookie(cookieUtil.createCookie("Authorization", newAccessToken));
    }

    @Transactional
    public void completeRegister(HttpServletResponse response, Long userId, JoinAdditionalInfoDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저 ID입니다. userId: " + userId));

        user.updateAdditionalInfo(
                Role.valueOf(requestDto.getRole()),
                districtRepository.findById(requestDto.getDistrictId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "존재하지 않는 지역입니다. districtId: " + requestDto.getDistrictId())
        ));

        String username = user.getUsername();
        String role = requestDto.getRole();
        String accessToken = jwtUtil.createAccessToken(userId, username, role);
        String refreshToken = jwtUtil.createRefreshToken(userId, username, role);

        redisUtil.saveRefreshToken(userId.toString(), refreshToken, 60 * 60 * 10000L);

        response.addCookie(cookieUtil.createCookie("Refresh-Token", refreshToken));
        response.addCookie(cookieUtil.createCookie("Authorization", accessToken));

        log.info("User Role Updated. Access Token: {}", accessToken);
    }

    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);

        redisUtil.deleteRefreshToken(userId.toString());
    }

    public void logout(String refreshToken, HttpServletResponse response) {
        Long userId = jwtUtil.getUserId(refreshToken);
        redisUtil.deleteRefreshToken(userId.toString());

        // 쿠키 삭제
        Cookie accessTokenCookie = new Cookie("Authorization", null);
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setHttpOnly(true);

        Cookie refreshTokenCookie = new Cookie("Refresh-Token", null);
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
    }

    public CheckLoginDto checkLogin(CustomOAuth2User user) {

        User users = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저 ID입니다. userId: " + user.getUserId()));

        return CheckLoginDto.builder()
                .isLogin(true)
                .userId(user.getUserId())
                .role(user.getRole())
                .notificationEnabled(users.getNotificationEnabled())
                .build();
    }
}
