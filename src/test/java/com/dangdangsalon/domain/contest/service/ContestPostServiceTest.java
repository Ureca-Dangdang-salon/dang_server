package com.dangdangsalon.domain.contest.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.dangdangsalon.domain.contest.dto.ContestJoinRequestDto;
import com.dangdangsalon.domain.contest.dto.PostInfoDto;
import com.dangdangsalon.domain.contest.entity.Contest;
import com.dangdangsalon.domain.contest.entity.ContestPost;
import com.dangdangsalon.domain.contest.repository.ContestPostRepository;
import com.dangdangsalon.domain.contest.repository.ContestRepository;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.groomerprofile.repository.GroomerProfileRepository;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ContestPostServiceTest {

    @Mock
    private ContestRepository contestRepository;

    @Mock
    private ContestPostRepository contestPostRepository;

    @Mock
    private GroomerProfileRepository groomerProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ContestPostLikeService contestPostLikeService;

    @InjectMocks
    private ContestPostService contestPostService;

    private Contest mockContest;
    private GroomerProfile mockGroomerProfile;
    private User mockUser;
    private ContestPost mockPost;

    @BeforeEach
    void setUp() {
        mockContest = Contest.builder()
                .title("콘테스트")
                .startedAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusDays(7))
                .build();

        mockGroomerProfile = GroomerProfile.builder()
                .name("미용사")
                .build();

        mockUser = User.builder()
                .name("유저")
                .build();

        mockPost = ContestPost.builder()
                .description("강아지 이쁘다")
                .contest(mockContest)
                .groomerProfile(mockGroomerProfile)
                .user(mockUser)
                .build();
    }

    @Test
    @DisplayName("콘테스트 게시글 조회")
    void getContestPosts() {
        Page<ContestPost> mockPosts = new PageImpl<>(List.of(mockPost));

        ReflectionTestUtils.setField(mockPost, "id", 1L);

        given(contestPostRepository.findByContestId(anyLong(), any(Pageable.class))).willReturn(mockPosts);
        given(contestPostLikeService.checkIsLiked(anyLong(), anyLong())).willReturn(true);

        Page<PostInfoDto> result = contestPostService.getContestPosts(1L, 1L, PageRequest.of(0, 5));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).isLiked()).isTrue();

        verify(contestPostRepository, times(1)).findByContestId(anyLong(), any(Pageable.class));
        verify(contestPostLikeService, times(1)).checkIsLiked(anyLong(), anyLong());
    }

    @Test
    @DisplayName("콘테스트 참여")
    void joinContest() {
        ContestJoinRequestDto requestDto = ContestJoinRequestDto.builder().contestId(1L).imageUrl(null)
                .description("description").groomerProfileId(1L).build();

        given(contestRepository.findById(anyLong())).willReturn(Optional.of(mockContest));
        given(groomerProfileRepository.findById(anyLong())).willReturn(Optional.of(mockGroomerProfile));
        given(userRepository.findById(anyLong())).willReturn(Optional.of(mockUser));
        given(contestPostRepository.existsByContestIdAndUserId(anyLong(), anyLong())).willReturn(false);

        contestPostService.joinContest(requestDto, 1L);

        verify(contestRepository, times(1)).findById(anyLong());
        verify(groomerProfileRepository, times(1)).findById(anyLong());
        verify(userRepository, times(1)).findById(anyLong());
        verify(contestPostRepository, times(1)).existsByContestIdAndUserId(anyLong(), anyLong());
        verify(contestPostRepository, times(1)).save(any(ContestPost.class));
    }

    @Test
    @DisplayName("콘테스트 참여 중복 예외")
    void joinContestAlreadyJoined() {
        ContestJoinRequestDto requestDto = ContestJoinRequestDto.builder().contestId(1L).imageUrl(null)
                .description("description").groomerProfileId(1L).build();

        given(contestRepository.findById(anyLong())).willReturn(Optional.of(mockContest));
        given(groomerProfileRepository.findById(anyLong())).willReturn(Optional.of(mockGroomerProfile));
        given(userRepository.findById(anyLong())).willReturn(Optional.of(mockUser));
        given(contestPostRepository.existsByContestIdAndUserId(anyLong(), anyLong())).willReturn(true);

        assertThrows(IllegalStateException.class, () -> contestPostService.joinContest(requestDto, 1L));

        verify(contestRepository, times(1)).findById(anyLong());
        verify(contestPostRepository, times(1)).existsByContestIdAndUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("게시글 삭제 - 성공")
    void deletePost() {
        ReflectionTestUtils.setField(mockUser, "id", 1L);

        given(contestPostRepository.findById(anyLong())).willReturn(Optional.of(mockPost));

        contestPostService.deletePost(1L, mockUser.getId());

        verify(contestPostRepository, times(1)).findById(anyLong());
        verify(contestPostRepository, times(1)).delete(any(ContestPost.class));
    }

    @Test
    @DisplayName("게시글 삭제 - 작성자 불일치 예외")
    void deletePostInvalidUser() {
        User otherUser = mock(User.class);
        User writer = mock(User.class);
        mockPost = mock(ContestPost.class);

        given(contestPostRepository.findById(anyLong())).willReturn(Optional.of(mockPost));
        given(mockPost.getUser()).willReturn(writer);
        given(mockPost.getUser().getId()).willReturn(1L);

        assertThrows(IllegalStateException.class, () -> contestPostService.deletePost(1L, otherUser.getId()));

        verify(contestPostRepository, times(1)).findById(anyLong());
        verify(contestPostRepository, times(0)).delete(any(ContestPost.class));
    }
}