package com.dangdangsalon.domain.contest.service;

import com.dangdangsalon.domain.contest.dto.ContestDetailDto;
import com.dangdangsalon.domain.contest.dto.ContestInfoDto;
import com.dangdangsalon.domain.contest.dto.LastContestWinnerDto;
import com.dangdangsalon.domain.contest.dto.PostRankDto;
import com.dangdangsalon.domain.contest.dto.SimpleWinnerInfoDto;
import com.dangdangsalon.domain.contest.dto.WinnerRankDto;
import com.dangdangsalon.domain.contest.entity.Contest;
import com.dangdangsalon.domain.contest.entity.ContestPost;
import com.dangdangsalon.domain.contest.repository.ContestPostLikeRepository;
import com.dangdangsalon.domain.contest.repository.ContestPostRepository;
import com.dangdangsalon.domain.contest.repository.ContestRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContestService {

    private final ContestRepository contestRepository;
    private final ContestPostRepository contestPostRepository;
    private final ContestPostLikeRepository contestPostLikeRepository;

    @Transactional(readOnly = true)
    public ContestInfoDto getLatestContest() {
        Contest latestContest = contestRepository.findTopByOrderByStartedAtDesc()
                .orElseThrow(() -> new IllegalArgumentException("진행중인 콘테스트가 없습니다."));

        return ContestInfoDto.fromEntity(latestContest);
    }

    @Transactional(readOnly = true)
    public ContestDetailDto getContestDetails(Long contestId) {
        Contest nowContest = contestRepository.findById(contestId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 콘테스트가 없습니다. InputId: " + contestId));

        Contest previousContest = contestRepository.findPreviousContest()
                .orElseThrow(() -> new IllegalArgumentException("이전 콘테스트가 없습니다."));

        SimpleWinnerInfoDto previousWinnerDto = null;

        if (previousContest.getWinnerPost() != null) {
            previousWinnerDto = SimpleWinnerInfoDto.fromEntity(previousContest.getWinnerPost());
        }

        return ContestDetailDto.create(nowContest, previousWinnerDto);
    }

    @Transactional(readOnly = true)
    public boolean checkUserParticipated(Long contestId, Long userId) {
        return contestPostRepository.existsByContestIdAndUserId(contestId, userId);
    }

    @Transactional(readOnly = true)
    public LastContestWinnerDto getLastContestWinner() {
        Contest lastContest = contestRepository.findPreviousContest()
                .orElseThrow(() -> new IllegalArgumentException("지난달 콘테스트가 존재하지 않습니다."));

        return LastContestWinnerDto.fromEntity(lastContest);
    }

    @Transactional(readOnly = true)
    public WinnerRankDto getWinnerAndRankPost() {
        Contest previousContest = contestRepository.findPreviousContest()
                .orElseThrow(() -> new IllegalArgumentException("지난 콘테스트가 없습니다."));

        ContestPost winnerPost = previousContest.getWinnerPost();
        if (winnerPost == null) {
            throw new IllegalArgumentException("지난 콘테스트의 우승자가 없습니다.");
        }

        Long winnerLikeCount = contestPostLikeRepository.getLikeCountByPostId(winnerPost.getId());

        PostRankDto winnerDto = PostRankDto.builder()
                .postId(winnerPost.getId())
                .userId(winnerPost.getUser().getId())
                .dogName(winnerPost.getDogName())
                .imageUrl(winnerPost.getImageKey())
                .likeCount(winnerLikeCount + 99)
                .build();

        Pageable pageable = PageRequest.of(0, 6);
        Page<PostRankDto> rankPostPage = contestPostRepository.findTopRankPostsByContestId(previousContest.getId(),
                pageable);

        List<PostRankDto> rankPosts = rankPostPage.getContent().stream()
                .filter(post -> !post.getPostId().equals(winnerPost.getId()))
                .limit(5)
                .toList();

        return WinnerRankDto.create(previousContest.getId(), winnerDto, rankPosts);
    }

    @Transactional
    public void savePreviousContestWinner() {
        Contest previousContest = contestRepository.findPreviousContest()
                .orElseThrow(() -> new IllegalArgumentException("지난 달에 진행된 콘테스트가 없습니다."));

        ContestPost winnerPost = contestPostRepository.findTopLikedPostByContestId(previousContest.getId()).get(0);

        previousContest.updateWinner(winnerPost);
    }
}
