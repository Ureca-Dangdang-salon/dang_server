package com.dangdangsalon.domain.contest.service;

import com.dangdangsalon.domain.contest.dto.ContestJoinRequestDto;
import com.dangdangsalon.domain.contest.dto.PostInfoDto;
import com.dangdangsalon.domain.contest.entity.Contest;
import com.dangdangsalon.domain.contest.entity.ContestPost;
import com.dangdangsalon.domain.contest.repository.ContestPostLikeRepository;
import com.dangdangsalon.domain.contest.repository.ContestPostRepository;
import com.dangdangsalon.domain.contest.repository.ContestRepository;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.groomerprofile.repository.GroomerProfileRepository;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContestPostService {

    private final ContestRepository contestRepository;
    private final ContestPostRepository contestPostRepository;
    private final ContestPostLikeRepository contestPostLikeRepository;
    private final GroomerProfileRepository groomerProfileRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<PostInfoDto> getContestPosts(Long contestId, Long userId, Pageable pageable) {
        Page<ContestPost> posts = contestPostRepository.findByContestId(contestId, pageable);

        List<Long> postIds = posts.getContent().stream()
                .map(ContestPost::getId)
                .toList();

        List<Long> likedPostIds = contestPostLikeRepository.findLikedPostIdsByUserId(userId, postIds);

        return posts.map(post -> {
            boolean isLiked = likedPostIds.contains(post.getId());
            return PostInfoDto.create(post, isLiked);
        });
    }

    @Transactional
    public void joinContest(Long contestId, ContestJoinRequestDto requestDto, Long userId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 콘테스트가 없습니다. InputId: " + contestId));

        GroomerProfile groomerProfile = groomerProfileRepository.findById(requestDto.getGroomerProfileId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당하는 미용사 프로필이 없습니다. InputId: " + requestDto.getGroomerProfileId()));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 유저가 없습니다. InputId: " + userId));

        if (isAlreadyJoined(contestId, userId)) {
            throw new IllegalStateException("이미 해당 콘테스트에 참여하셨습니다.");
        }

        ContestPost joinPost = ContestPost.builder()
                .imageKey(requestDto.getImageUrl())
                .description(requestDto.getDescription())
                .contest(contest)
                .groomerProfile(groomerProfile)
                .user(user)
                .build();

        contestPostRepository.save(joinPost);
    }

    private boolean isAlreadyJoined(Long contestId, Long userId) {
        return contestPostRepository.existsByContestIdAndUserId(contestId, userId);
    }
}
