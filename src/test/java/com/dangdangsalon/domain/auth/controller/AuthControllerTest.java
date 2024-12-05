package com.dangdangsalon.domain.auth.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.auth.dto.JoinAdditionalInfoDto;
import com.dangdangsalon.domain.auth.service.AuthService;
import com.dangdangsalon.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
@MockBean(JpaMetamodelMappingContext.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CookieUtil cookieUtil;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("Access Token 갱신 테스트")
    void testRefreshAccessToken() throws Exception {
        CustomOAuth2User mockUser = mock(CustomOAuth2User.class);
        given(mockUser.getUserId()).willReturn(1L);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );

        String mockRefreshToken = "mockRefreshToken";
        String newAccessToken = "newAccessToken";
        given(cookieUtil.getCookieValue(eq("Refresh-Token"), any(HttpServletRequest.class))).willReturn(mockRefreshToken);

        doAnswer(invocation -> {
            HttpServletResponse response = invocation.getArgument(1);
            Cookie accessTokenCookie = new Cookie("Authorization", newAccessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(60 * 60 * 24 * 30);
            response.addCookie(accessTokenCookie);
            return null;
        }).when(authService).refreshAccessToken(eq(mockRefreshToken), any(HttpServletResponse.class));

        mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("액세스 토큰 갱신에 성공했습니다."));

        verify(cookieUtil, times(1)).getCookieValue(eq("Refresh-Token"), any(HttpServletRequest.class));
        verify(authService, times(1)).refreshAccessToken(eq(mockRefreshToken), any(HttpServletResponse.class));
    }

    @Test
    @WithMockUser(username = "user", roles = {"PENDING"})
    @DisplayName("회원가입 완료 테스트")
    void testCompleteSignup() throws Exception {
        JoinAdditionalInfoDto requestDto = JoinAdditionalInfoDto.builder()
                .role("ROLE_SALON")
                .districtId(1L)
                .build();

        CustomOAuth2User mockUser = mock(CustomOAuth2User.class);
        given(mockUser.getUserId()).willReturn(1L);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_PENDING")))
        );

        mockMvc.perform(post("/api/auth/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("회원가입에 성공했습니다."));

        verify(authService, times(1))
                .completeRegister(any(HttpServletResponse.class), eq(1L), any(JoinAdditionalInfoDto.class));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("회원탈퇴 테스트")
    void testDeleteUser() throws Exception {
        CustomOAuth2User mockUser = mock(CustomOAuth2User.class);
        given(mockUser.getUserId()).willReturn(1L);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );

        mockMvc.perform(delete("/api/auth/delete")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("회원탈퇴에 성공했습니다."));

        verify(authService, times(1)).deleteUser(eq(1L));
    }

    @Test
    @DisplayName("로그아웃 테스트")
    void testLogout() throws Exception {
        CustomOAuth2User mockUser = mock(CustomOAuth2User.class);
        given(mockUser.getUserId()).willReturn(1L);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );

        String mockRefreshToken = "mockRefreshToken";
        given(cookieUtil.getCookieValue(eq("Refresh-Token"), any(HttpServletRequest.class))).willReturn(mockRefreshToken);

        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("로그아웃이 완료되었습니다."));

        verify(cookieUtil, times(1)).getCookieValue(eq("Refresh-Token"), any(HttpServletRequest.class));
        verify(authService, times(1)).logout(eq(mockRefreshToken), any(HttpServletResponse.class));
    }
}