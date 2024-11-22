package com.dangdangsalon.domain.contest.dto;

import com.dangdangsalon.domain.contest.entity.ContestPost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostRankDto {

    private Long postId;
    private Long userId;
    private String dogName;
    private String imageUrl;
    private Long likeCount;

    public PostRankDto(Long postId, Long userId, String dogName, String imageUrl, Long likeCount) {
        this.postId = postId;
        this.userId = userId;
        this.dogName = dogName;
        this.imageUrl = imageUrl;
        this.likeCount = likeCount;
    }

    public static PostRankDto create(ContestPost post, Long likeCount) {
        return PostRankDto.builder()
                .postId(post.getId())
                .userId(post.getUser().getId())
                .dogName(post.getGroomerProfile().getName())
                .imageUrl(post.getImageKey())
                .likeCount(likeCount)
                .build();
    }
}
