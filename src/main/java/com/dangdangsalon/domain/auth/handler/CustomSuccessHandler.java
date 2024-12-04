package com.dangdangsalon.domain.auth.handler;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.user.entity.Role;
import com.dangdangsalon.util.CookieUtil;
import com.dangdangsalon.util.JwtUtil;
import com.dangdangsalon.util.RedisUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final CookieUtil cookieUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        String username = customUserDetails.getUsername();
        Long userId = customUserDetails.getUserId();
        String role = customUserDetails.getAuthorities().iterator().next().getAuthority();

        String accessToken = jwtUtil.createAccessToken(userId, username, role);
        String refreshToken = jwtUtil.createRefreshToken(userId, username, role);
        redisUtil.saveRefreshToken(userId.toString(), refreshToken, 60 * 60 * 10000L);

        response.addCookie(cookieUtil.createCookie("Refresh-Token", refreshToken));
        response.addCookie(cookieUtil.createCookie("Authorization", accessToken));

//        response.addHeader("Authorization", "Bearer " + accessToken);
        log.info("Access Token: {}", accessToken);
        log.info("Refresh Token: {}", refreshToken);

        String redirectUrl = role.equals(Role.ROLE_PENDING.name())
                ? "https://dangdangsalon.netlify.app/survey"
                : "https://dangdangsalon.netlify.app/home";

        response.sendRedirect(redirectUrl);
    }
}


