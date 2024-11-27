package com.dangdangsalon.domain.contest.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.dangdangsalon.domain.contest.dto.ContestDetailDto;
import com.dangdangsalon.domain.contest.dto.ContestInfoDto;
import com.dangdangsalon.domain.contest.dto.LastContestWinnerDto;
import com.dangdangsalon.domain.contest.dto.PostRankDto;
import com.dangdangsalon.domain.contest.dto.WinnerRankDto;
import com.dangdangsalon.domain.contest.entity.Contest;
import com.dangdangsalon.domain.contest.entity.ContestPost;
import com.dangdangsalon.domain.contest.repository.ContestPostLikeRepository;
import com.dangdangsalon.domain.contest.repository.ContestPostRepository;
import com.dangdangsalon.domain.contest.repository.ContestRepository;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ContestServiceTest {

    @Mock
    private ContestRepository contestRepository;

    @Mock
    private ContestPostRepository contestPostRepository;

    @Mock
    private ContestPostLikeRepository contestPostLikeRepository;

    @InjectMocks
    private ContestService contestService;

    private Contest mockContest;
    private ContestPost mockWinnerPost;

    @BeforeEach
    void setUp() {
        mockContest = Contest.builder()
                .title("콘테스트")
                .startedAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusDays(5))
                .build();

        mockWinnerPost = ContestPost.builder()
                .dogName("왕왕")
                .user(User.builder().name("유저").build())
                .groomerProfile(GroomerProfile.builder().name("미용사").build())
                .contest(mockContest)
                .build();
    }

    @Test
    @DisplayName("진행 중인 콘테스트 조회 테스트 - 성공")
    void testGetLatestContest_Success() {
        BDDMockito.given(contestRepository.findTopByOrderByStartedAtDesc()).willReturn(Optional.of(mockContest));

        ContestInfoDto result = contestService.getLatestContest();

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("콘테스트");

        verify(contestRepository, times(1)).findTopByOrderByStartedAtDesc();
    }

    @Test
    @DisplayName("진행 중인 콘테스트 조회 테스트 - 실패")
    void testGetLatestContest_Fail() {
        given(contestRepository.findTopByOrderByStartedAtDesc()).willReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> contestService.getLatestContest());
    }

    @Test
    @DisplayName("콘테스트 상세 조회 테스트")
    void testGetContestDetails() {
        Contest previousContest = Contest.builder()
                .title("이전 콘테스트")
                .winnerPost(mockWinnerPost)
                .build();

        given(contestRepository.findById(anyLong())).willReturn(Optional.of(mockContest));
        given(contestRepository.findPreviousContest()).willReturn(Optional.of(previousContest));

        ContestDetailDto result = contestService.getContestDetails(1L);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("콘테스트");
        assertThat(result.getRecentWinner()).isNotNull();

        verify(contestRepository, times(1)).findById(anyLong());
        verify(contestRepository, times(1)).findPreviousContest();
    }

    @Test
    @DisplayName("유저 참여 여부 확인 테스트")
    void testCheckUserParticipated() {
        given(contestPostRepository.existsByContestIdAndUserId(anyLong(), anyLong())).willReturn(true);

        boolean result = contestService.checkUserParticipated(1L, 1L);

        assertThat(result).isTrue();

        verify(contestPostRepository, times(1)).existsByContestIdAndUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("지난 콘테스트 우승자 조회 테스트 - 성공")
    void testGetLastContestWinner_Success() {
        Contest previousContest = Contest.builder()
                .title("이전 콘테스트")
                .winnerPost(mockWinnerPost)
                .build();

        given(contestRepository.findPreviousContest()).willReturn(Optional.of(previousContest));

        LastContestWinnerDto result = contestService.getLastContestWinner();

        assertThat(result).isNotNull();

        verify(contestRepository, times(1)).findPreviousContest();
    }

    @Test
    @DisplayName("지난 콘테스트 우승자 조회 테스트 - 실패")
    void testGetLastContestWinner_Fail() {
        given(contestRepository.findPreviousContest()).willReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> contestService.getLastContestWinner());
    }

    @Test
    @DisplayName("우승자 및 순위 게시글 조회 테스트")
    void testGetWinnerAndRankPost() {
        Contest previousContest = mock(Contest.class);

        PostRankDto rank1 = new PostRankDto(1L, 1L, "Dog 1", "image1", 10L);
        PostRankDto rank2 = new PostRankDto(2L, 2L, "Dog 2", "image2", 8L);

        Page<PostRankDto> rankPage = new PageImpl<>(List.of(rank1, rank2));

        given(contestRepository.findPreviousContest()).willReturn(Optional.of(previousContest));
        given(previousContest.getId()).willReturn(1L);
        given(previousContest.getWinnerPost()).willReturn(mockWinnerPost);
        given(contestPostLikeRepository.getLikeCountByPostId(mockWinnerPost.getId())).willReturn(12L);
        given(contestPostRepository.findTopRankPostsByContestId(anyLong(), any(Pageable.class))).willReturn(rankPage);

        WinnerRankDto result = contestService.getWinnerAndRankPost();

        assertThat(result).isNotNull();
        assertThat(result.getWinnerPost().getDogName()).isEqualTo("왕왕");
        assertThat(result.getRankPosts()).hasSize(2);

        verify(contestRepository, times(1)).findPreviousContest();
        verify(contestPostLikeRepository, times(1)).getLikeCountByPostId(mockWinnerPost.getId());
        verify(contestPostRepository, times(1)).findTopRankPostsByContestId(anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("지난 콘테스트 우승자 저장 테스트")
    void testSavePreviousContestWinner() {
        Contest previousContest = mock(Contest.class);

        given(contestRepository.findPreviousContest()).willReturn(Optional.of(previousContest));
        given(contestPostRepository.findTopLikedPostByContestId(anyLong()))
                .willReturn(List.of(mockWinnerPost));

        contestService.savePreviousContestWinner();

        verify(contestRepository, times(1)).findPreviousContest();
        verify(contestPostRepository, times(1)).findTopLikedPostByContestId(anyLong());
    }
}