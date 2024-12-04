package com.dangdangsalon.domain.mypage.controller;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.mypage.dto.req.CommonProfileRequestDto;
import com.dangdangsalon.domain.mypage.dto.res.CommonProfileResponseDto;
import com.dangdangsalon.domain.mypage.service.MyPageCommonService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@WebMvcTest(controllers = MyPageCommonController.class)
@MockBean(JpaMetamodelMappingContext.class)
class MyPageCommonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MyPageCommonService myPageCommonService;

    @Test
    @DisplayName("유저 프로필 조회 테스트")
    void getUserProfileTest() throws Exception {
        // Mock CustomOAuth2User 설정
        Long mockUserId = 1L;
        CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
        when(customOAuth2User.getUserId()).thenReturn(mockUserId);

        // SecurityContext에 사용자 설정
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                customOAuth2User,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")) // 권한 추가
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 유저 프로필 데이터 예시
        CommonProfileResponseDto mockResponse = new CommonProfileResponseDto("imageKey",
                "name", "email@example.com", "종로구", "서울시");
        when(myPageCommonService.getUserinfo(mockUserId)).thenReturn(mockResponse);

        // GET 요청으로 유저 프로필 조회
        mockMvc.perform(get("/api/common")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                .principal(new UsernamePasswordAuthenticationToken(customOAuth2User, null)))  // CSRF 토큰을 포함하여 요청
                .andExpect(status().isOk())  // 상태 코드 200 OK 확인
                .andExpect(jsonPath("$.response.imageKey").value("imageKey"))  // 사용자 이름 확인
                .andExpect(jsonPath("$.response.email").value("email@example.com"));  // 사용자 이메일 확인
    }

    @Test
    @DisplayName("유저 프로필 업데이트 테스트")
    void updateUserProfileTest() throws Exception {
        // Mock CustomOAuth2User 설정
        Long mockUserId = 1L;
        CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
        when(customOAuth2User.getUserId()).thenReturn(mockUserId);

        // SecurityContext에 사용자 설정
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                customOAuth2User,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")) // 권한 추가
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 업데이트할 프로필 데이터
        CommonProfileRequestDto requestDto = new CommonProfileRequestDto("imageKey",
                "newEmail@example.com", 1L);

        // 프로필 업데이트 서비스 모킹
        // POST 요청으로 유저 프로필 업데이트
        mockMvc.perform(put("/api/common")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto))
                        .with(csrf())
                        .principal(new UsernamePasswordAuthenticationToken(customOAuth2User, null)))
                .andExpect(status().isOk())  // 상태 코드 200 OK 확인
                .andExpect(jsonPath("$.response").value("유저 정보가 변경되었습니다."));  // 응답 메시지 확인
    }
}