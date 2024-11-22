package com.dangdangsalon.domain.contest.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.contest.dto.ContestJoinRequestDto;
import com.dangdangsalon.domain.contest.service.ContestPostLikeService;
import com.dangdangsalon.domain.contest.service.ContestPostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(ContestPostController.class)
@ActiveProfiles("test")
@MockBean(JpaMetamodelMappingContext.class)
class ContestPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContestPostService contestPostService;

    @MockBean
    private ContestPostLikeService contestPostLikeService;

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("콘테스트 참여 테스트")
    void testJoinContest() throws Exception {
        ContestJoinRequestDto requestDto = ContestJoinRequestDto.builder()
                .contestId(1L)
                .groomerProfileId(2L)
                .imageUrl("imageUrl")
                .description("description")
                .dogName("멍멍")
                .build();

        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("콘테스트 참여에 성공했습니다!"));

        verify(contestPostService, times(1)).joinContest(any(ContestJoinRequestDto.class), anyLong());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("포스트 삭제 테스트")
    void testDeletePost() throws Exception {
        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        mockMvc.perform(delete("/api/posts/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("포스트 삭제가 완료되었습니다."));

        verify(contestPostService, times(1)).deletePost(eq(1L), anyLong());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("좋아요 누르기 테스트")
    void testLikePost() throws Exception {
        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        mockMvc.perform(post("/api/posts/1/like")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("해당 게시물에 좋아요를 눌렀습니다"));

        verify(contestPostLikeService, times(1)).likePost(eq(1L), anyLong());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("좋아요 취소 테스트")
    void testUnlikePost() throws Exception {
        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        mockMvc.perform(delete("/api/posts/1/like")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("좋아요를 취소했습니다."));

        verify(contestPostLikeService, times(1)).unlikePost(eq(1L), anyLong());
    }
}