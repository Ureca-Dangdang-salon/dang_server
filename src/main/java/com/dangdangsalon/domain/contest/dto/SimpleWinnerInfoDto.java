package com.dangdangsalon.domain.contest.dto;

import com.dangdangsalon.domain.contest.entity.ContestPost;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SimpleWinnerInfoDto {
    private Long postId;
    private String userName;
    private String dogName;
    private String imageUrl;

    public static SimpleWinnerInfoDto fromEntity(ContestPost winnerPost) {
        return SimpleWinnerInfoDto.builder()
                .postId(winnerPost.getId())
                .userName(winnerPost.getUser().getName())
                .dogName(winnerPost.getDogName())
                .imageUrl(winnerPost.getImageKey())
                .build();
    }
}
