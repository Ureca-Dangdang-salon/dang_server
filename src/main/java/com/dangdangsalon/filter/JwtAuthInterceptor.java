package com.dangdangsalon.filter;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.auth.dto.UserDto;
import com.dangdangsalon.exception.TokenExpiredException;
import com.dangdangsalon.util.JwtUtil;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String cookieHeader = (String) Objects.requireNonNull(accessor.getSessionAttributes()).get("cookie");
            String token = extractTokenFromCookie(cookieHeader);

            if (token != null && !jwtUtil.isExpired(token)) {
                Long userId = jwtUtil.getUserId(token);
                String username = jwtUtil.getUsername(token);
                String role = jwtUtil.getRole(token);

                log.info("userId: {}, username: {}, role: {}", userId, username, role);

                UserDto userDto = UserDto.builder()
                        .userId(userId)
                        .username(username)
                        .role(role)
                        .build();

                CustomOAuth2User customOAuth2User = new CustomOAuth2User(userDto);

                Authentication auth = new UsernamePasswordAuthenticationToken(customOAuth2User, null,
                        customOAuth2User.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(auth);
                accessor.setUser(auth);
            } else {
                throw new TokenExpiredException();
            }
        }

        return message;
    }

    private String extractTokenFromCookie(String cookieHeader) {
        return Arrays.stream(cookieHeader.split(";"))
                .map(String::trim)
                .filter(cookie -> cookie.startsWith("Authorization="))
                .map(cookie -> cookie.substring("Authorization=".length()))
                .findFirst()
                .orElse(null);
    }
}
