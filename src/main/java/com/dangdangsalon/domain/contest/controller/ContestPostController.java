package com.dangdangsalon.domain.contest.controller;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.contest.dto.ContestJoinRequestDto;
import com.dangdangsalon.domain.contest.service.ContestPostLikeService;
import com.dangdangsalon.domain.contest.service.ContestPostService;
import com.dangdangsalon.util.ApiUtil;
import com.dangdangsalon.util.ApiUtil.ApiSuccess;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class ContestPostController {

    private final ContestPostService contestPostService;
    private final ContestPostLikeService contestPostLikeService;

    @PostMapping
    public ApiSuccess<?> joinContest(
            @RequestBody ContestJoinRequestDto requestDto,
            @AuthenticationPrincipal CustomOAuth2User user) {

        Long userId = user.getUserId();
        contestPostService.joinContest(requestDto, userId);

        return ApiUtil.success("콘테스트 참여에 성공했습니다!");
    }

    @DeleteMapping("/{postId}")
    public ApiSuccess<?> deletePost(@PathVariable Long postId, @AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();
        contestPostService.deletePost(postId, userId);

        return ApiUtil.success("포스트 삭제가 완료되었습니다.");
    }

    @PostMapping("/{postId}/like")
    public ApiSuccess<?> likePost(@PathVariable Long postId, @AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();
        contestPostLikeService.likePost(postId, userId);

        return ApiUtil.success("해당 게시물에 좋아요를 눌렀습니다");
    }

    @DeleteMapping("/{postId}/like")
    public ApiSuccess<?> unlikePost(@PathVariable Long postId, @AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();
        contestPostLikeService.unlikePost(postId, userId);

        return ApiUtil.success("좋아요를 취소했습니다.");
    }
}
