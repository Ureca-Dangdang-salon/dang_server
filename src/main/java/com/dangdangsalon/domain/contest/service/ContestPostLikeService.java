package com.dangdangsalon.domain.contest.service;

import com.dangdangsalon.domain.contest.entity.ContestPost;
import com.dangdangsalon.domain.contest.entity.ContestPostLike;
import com.dangdangsalon.domain.contest.repository.ContestPostLikeRepository;
import com.dangdangsalon.domain.contest.repository.ContestPostRepository;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContestPostLikeService {

    private final ContestPostRepository contestPostRepository;
    private final ContestPostLikeRepository contestPostLikeRepository;
    private final UserRepository userRepository;

    @Transactional
    @CacheEvict(value = "likeStatus", key = "'like_status:' + #userId + ':' + #postId")
    public void likePost(Long postId, Long userId) {
        if (isLike(postId, userId)) {
            throw new IllegalStateException("이미 좋아요를 눌렀습니다.");
        }

        ContestPost post = contestPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 포스트가 존재하지 않습니다. postId: " + postId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. userId: " + userId));

        ContestPostLike like = ContestPostLike.builder()
                .user(user)
                .contestPost(post)
                .build();

        contestPostLikeRepository.save(like);
    }

    @Transactional
    @CacheEvict(value = "likeStatus", key = "'like_status:' + #userId + ':' + #postId")
    public void unlikePost(Long postId, Long userId) {
        if (!isLike(postId, userId)) {
            throw new IllegalStateException("좋아요를 누르지 않았습니다");
        }

        contestPostLikeRepository.deleteByUserIdAndContestPostId(userId, postId);
    }

    @Cacheable(value = "likeStatus", key = "'like_status:' + #userId + ':' + #postId")
    public boolean checkIsLiked(Long postId, Long userId) {
        return contestPostLikeRepository.existsByUserIdAndContestPostId(userId, postId);
    }

    private boolean isLike(Long postId, Long userId) {
        return contestPostLikeRepository.existsByUserIdAndContestPostId(userId, postId);
    }
}
