package com.dangdangsalon.domain.contest.dto;

import com.dangdangsalon.domain.contest.entity.ContestPost;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostInfoDto {

    private Long postId;
    private Long userId;
    private String userProfileImage;
    private String userName;
    private String dogName;
    private String imageUrl;
    private String description;
    private LocalDateTime createdAt;
    private boolean isLiked;

    public static PostInfoDto create(ContestPost post, boolean isLiked) {
        return PostInfoDto.builder()
                .postId(post.getId())
                .userId(post.getUser().getId())
                .userProfileImage(post.getUser().getImageKey())
                .userName(post.getUser().getName())
                .dogName(post.getDogName())
                .imageUrl(post.getImageKey())
                .description(post.getDescription())
                .createdAt(post.getCreatedAt())
                .isLiked(isLiked)
                .build();
    }
}
