package com.dangdangsalon.domain.mypage.controller;

import com.dangdangsalon.domain.groomerprofile.entity.ServiceType;
import com.dangdangsalon.domain.mypage.dto.req.GroomerDetailsUpdateRequestDto;
import com.dangdangsalon.domain.mypage.dto.req.GroomerProfileDetailsRequestDto;
import com.dangdangsalon.domain.mypage.dto.req.GroomerProfileRequestDto;
import com.dangdangsalon.domain.mypage.dto.res.GroomerProfileDetailsResponseDto;
import com.dangdangsalon.domain.mypage.dto.res.GroomerProfileResponseDto;
import com.dangdangsalon.domain.mypage.service.MyPageGroomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;


import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
@WebMvcTest(controllers = MyPageGroomerController.class)
@MockBean(JpaMetamodelMappingContext.class)
class MyPageGroomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MyPageGroomerService myPageGroomerService;

    @Test
    @DisplayName("미용사 프로필 페이지 조회")
    void getGroomerProfilePageTest() throws Exception {
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

        GroomerProfileResponseDto responseDto = GroomerProfileResponseDto.builder()
                .name("Groomer")
                .profileImage("profileImage123")
                .district("종로구")
                .build();
        when(myPageGroomerService.getGroomerProfilePage(mockUserId)).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(get("/api/groomerprofile")
                        .principal(new UsernamePasswordAuthenticationToken(customOAuth2User, null))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.name").value("Groomer"))
                .andExpect(jsonPath("$.response.profileImage").value("profileImage123"))
                .andExpect(jsonPath("$.response.district").value("종로구"));
    }

    @Test
    @DisplayName("미용사 프로필 상세 조회")
    void getGroomerProfileDetailTest() throws Exception {
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

        // Given
        Long profileId = 10L;
        GroomerProfileDetailsResponseDto responseDto = GroomerProfileDetailsResponseDto.builder()
                .serviceName("Groomer")
                .description("Specialist in grooming")
                .contact("010-1234-1234")
                .build();

        when(myPageGroomerService.getGroomerProfile(profileId)).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(get("/api/groomerprofile/{profileId}", profileId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.serviceName").value("Groomer"))
                .andExpect(jsonPath("$.response.description").value("Specialist in grooming"))
                .andExpect(jsonPath("$.response.contact").value("010-1234-1234"));
    }

    @Test
    @DisplayName("미용사 프로필 등록")
    void saveGroomerProfileTest() throws Exception {
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

        GroomerProfileRequestDto requestDto = new GroomerProfileRequestDto();
        String requestContent = new ObjectMapper().writeValueAsString(requestDto);

        // When & Then
        mockMvc.perform(post("/api/groomerprofile")
                        .content(requestContent)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .principal(new UsernamePasswordAuthenticationToken(customOAuth2User, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("미용사 프로필 등록이 완료되었습니다."));
    }

    @Test
    @DisplayName("미용사 프로필 상세 등록")
    void saveGroomerProfileDetailTest() throws Exception {
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

        GroomerProfileDetailsRequestDto requestDto = new GroomerProfileDetailsRequestDto();

        // When & Then
        mockMvc.perform(post("/api/groomerprofile/detail")
                        .content(new ObjectMapper().writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .principal(new UsernamePasswordAuthenticationToken(customOAuth2User, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("미용사 프로필 상세 정보 등록이 완료되었습니다."));
    }

    @Test
    @DisplayName("미용사 프로필 수정")
    void updateGroomerProfileTest() throws Exception {
        // Mock CustomOAuth2User 설정
        Long mockUserId = 1L;
        Long profileId = 10L;
        CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
        when(customOAuth2User.getUserId()).thenReturn(mockUserId);

        // SecurityContext에 사용자 설정
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                customOAuth2User,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")) // 권한 추가
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);


        GroomerDetailsUpdateRequestDto requestDto = new GroomerDetailsUpdateRequestDto();
        String requestContent = new ObjectMapper().writeValueAsString(requestDto);

        // When & Then
        mockMvc.perform(put("/api/groomerprofile/{profileId}", profileId)
                        .content(requestContent)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .principal(new UsernamePasswordAuthenticationToken(customOAuth2User, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("미용사 프로필 상세 정보 등록이 완료되었습니다."));
    }

    @Test
    @DisplayName("미용사 프로필 삭제")
    void deleteGroomerProfileTest() throws Exception {
        // Mock CustomOAuth2User 설정
        Long mockUserId = 1L;
        Long profileId = 10L;
        CustomOAuth2User customOAuth2User = mock(CustomOAuth2User.class);
        when(customOAuth2User.getUserId()).thenReturn(mockUserId);

        // SecurityContext에 사용자 설정
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                customOAuth2User,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")) // 권한 추가
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // When & Then
        mockMvc.perform(delete("/api/groomerprofile/{profileId}", profileId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .principal(new UsernamePasswordAuthenticationToken(customOAuth2User, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("미용사 프로필 삭제가 완료되었습니다."));
    }
}