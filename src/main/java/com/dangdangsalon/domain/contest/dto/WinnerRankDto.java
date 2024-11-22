package com.dangdangsalon.domain.contest.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WinnerRankDto {

    private Long contestId;
    private PostRankDto winnerPost;
    private List<PostRankDto> rankPosts;

    public static WinnerRankDto create(Long contestId, PostRankDto winnerPost, List<PostRankDto> rankPosts) {
        return WinnerRankDto.builder()
                .contestId(contestId)
                .winnerPost(winnerPost)
                .rankPosts(rankPosts)
                .build();
    }
}
