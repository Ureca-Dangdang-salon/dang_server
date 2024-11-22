package com.dangdangsalon.domain.auth.service;

import com.dangdangsalon.domain.auth.dto.JoinAdditionalInfoDto;
import com.dangdangsalon.domain.region.repository.DistrictRepository;
import com.dangdangsalon.domain.user.entity.Role;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import com.dangdangsalon.exception.TokenExpiredException;
import com.dangdangsalon.util.JwtUtil;
import com.dangdangsalon.util.RedisUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final UserRepository userRepository;
    private final DistrictRepository districtRepository;

    public Map<String, String> refreshAccessToken(String refreshToken) {
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

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("accessToken", newAccessToken);

        return responseBody;
    }

    @Transactional
    public void completeRegister(Long userId, JoinAdditionalInfoDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저 ID입니다. userId: " + userId));

        user.updateAdditionalInfo(
                Role.valueOf(requestDto.getRole()),
                districtRepository.findById(requestDto.getDistrictId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "존재하지 않는 지역입니다. districtId: " + requestDto.getDistrictId())
        ));
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
}