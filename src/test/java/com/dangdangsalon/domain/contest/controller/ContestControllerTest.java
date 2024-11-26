package com.dangdangsalon.domain.contest.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.contest.dto.ContestDetailDto;
import com.dangdangsalon.domain.contest.dto.ContestInfoDto;
import com.dangdangsalon.domain.contest.dto.LastContestWinnerDto;
import com.dangdangsalon.domain.contest.dto.PostInfoDto;
import com.dangdangsalon.domain.contest.dto.PostRankDto;
import com.dangdangsalon.domain.contest.dto.SimpleWinnerInfoDto;
import com.dangdangsalon.domain.contest.dto.WinnerRankDto;
import com.dangdangsalon.domain.contest.entity.Contest;
import com.dangdangsalon.domain.contest.service.ContestPostService;
import com.dangdangsalon.domain.contest.service.ContestService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(ContestController.class)
@ActiveProfiles("test")
@MockBean(JpaMetamodelMappingContext.class)
class ContestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContestService contestService;

    @MockBean
    private ContestPostService contestPostService;

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("최신 콘테스트 조회")
    void testGetLatestContest() throws Exception {
        ContestInfoDto contestInfoDto = ContestInfoDto.builder()
                .title("최신콘테스트")
                .startedAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusDays(5))
                .build();

        given(contestService.getLatestContest()).willReturn(contestInfoDto);

        mockMvc.perform(get("/api/contests")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.title").value("최신콘테스트"));

        verify(contestService, times(1)).getLatestContest();
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("콘테스트 상세 조회")
    void testGetContestDetails() throws Exception {
        ContestDetailDto detailDto = ContestDetailDto.create(
                Contest.builder()
                        .title("콘테스트")
                        .description("설명")
                        .startedAt(LocalDateTime.now())
                        .endAt(LocalDateTime.now().plusDays(5))
                        .build(),
                SimpleWinnerInfoDto.builder()
                        .dogName("우승강아지")
                        .imageUrl("winnerDog.jpg")
                        .build()
        );

        given(contestService.getContestDetails(anyLong())).willReturn(detailDto);

        mockMvc.perform(get("/api/contests/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.title").value("콘테스트"))
                .andExpect(jsonPath("$.response.recentWinner.dogName").value("우승강아지"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("콘테스트 참여 여부 확인")
    void testCheckAlreadyJoin() throws Exception {
        CustomOAuth2User mockLoginUser = mock(CustomOAuth2User.class);
        given(mockLoginUser.getUserId()).willReturn(1L);
        given(contestService.checkUserParticipated(anyLong(), eq(1L))).willReturn(true);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        mockMvc.perform(get("/api/contests/1/check")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.already_participated").value(true));

        verify(contestService, times(1)).checkUserParticipated(anyLong(), anyLong());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("지난 콘테스트 우승자 조회")
    void testGetLastContestWinner() throws Exception {
        LastContestWinnerDto winnerDto = LastContestWinnerDto.builder()
                .contestId(1L)
                .post(PostInfoDto.builder()
                        .dogName("우승 강아지")
                        .build())
                .build();
        given(contestService.getLastContestWinner()).willReturn(winnerDto);

        mockMvc.perform(get("/api/contests/winner/last")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.post.dogName").value("우승 강아지"));

        verify(contestService, times(1)).getLastContestWinner();
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("우승자 및 순위 게시글 조회")
    void testGetWinnerAndRankPost() throws Exception {
        WinnerRankDto rankDto = WinnerRankDto.builder()
                .contestId(1L)
                .winnerPost(new PostRankDto(1L, 1L, "우승 강아지", "winner.jpg", 100L))
                .rankPosts(List.of(
                        new PostRankDto(2L, 2L, "Dog 2", "dog2.jpg", 90L),
                        new PostRankDto(3L, 3L, "Dog 3", "dog3.jpg", 80L)
                ))
                .build();
        given(contestService.getWinnerAndRankPost()).willReturn(rankDto);

        mockMvc.perform(get("/api/contests/winner/rank")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.winnerPost.dogName").value("우승 강아지"))
                .andExpect(jsonPath("$.response.rankPosts[0].dogName").value("Dog 2"));

        verify(contestService, times(1)).getWinnerAndRankPost();
    }
}