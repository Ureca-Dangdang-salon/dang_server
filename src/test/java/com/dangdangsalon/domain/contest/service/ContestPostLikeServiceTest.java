package com.dangdangsalon.domain.contest.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.dangdangsalon.domain.contest.entity.ContestPost;
import com.dangdangsalon.domain.contest.entity.ContestPostLike;
import com.dangdangsalon.domain.contest.repository.ContestPostLikeRepository;
import com.dangdangsalon.domain.contest.repository.ContestPostRepository;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContestPostLikeServiceTest {

    @Mock
    private ContestPostRepository contestPostRepository;

    @Mock
    private ContestPostLikeRepository contestPostLikeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ContestPostLikeService contestPostLikeService;

    private ContestPost mockPost;
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .name("유저")
                .build();

        mockPost = ContestPost.builder()
                .dogName("왕왕")
                .description("소개문구")
                .build();
    }

    @Test
    @DisplayName("좋아요 성공 테스트")
    void likePost() {
        given(contestPostLikeRepository.existsByUserIdAndContestPostId(anyLong(), anyLong())).willReturn(false);
        given(contestPostRepository.findById(anyLong())).willReturn(Optional.of(mockPost));
        given(userRepository.findById(anyLong())).willReturn(Optional.of(mockUser));

        contestPostLikeService.likePost(1L, 1L);

        verify(contestPostLikeRepository, times(1)).save(any(ContestPostLike.class));
    }

    @Test
    @DisplayName("좋아요 취소 성공")
    void unlikePostSuccess() {
        given(contestPostLikeRepository.existsByUserIdAndContestPostId(anyLong(), anyLong())).willReturn(true);

        contestPostLikeService.unlikePost(1L, 1L);

        verify(contestPostLikeRepository, times(1)).deleteByUserIdAndContestPostId(1L, 1L);
    }

    @Test
    @DisplayName("좋아요 상태 확인 - 좋아요를 누른 상태")
    void checkIsLikedTrue() {
        given(contestPostLikeRepository.existsByUserIdAndContestPostId(anyLong(), anyLong())).willReturn(true);

        boolean result = contestPostLikeService.checkIsLiked(1L, 1L);

        assertThat(result).isTrue();
        verify(contestPostLikeRepository, times(1)).existsByUserIdAndContestPostId(1L, 1L);
    }

    @Test
    @DisplayName("좋아요 상태 확인 - 좋아요를 누르지 않은 상태")
    void checkIsLikedFalse() {
        given(contestPostLikeRepository.existsByUserIdAndContestPostId(anyLong(), anyLong())).willReturn(false);

        boolean result = contestPostLikeService.checkIsLiked(1L, 1L);

        assertThat(result).isFalse();
        verify(contestPostLikeRepository, times(1)).existsByUserIdAndContestPostId(1L, 1L);
    }
}