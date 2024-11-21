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
    private String userName;
    private String dogName;
    private String imageUrl;
    private String description;
    private LocalDateTime createdAt;

    public static PostInfoDto fromEntity(ContestPost post) {
        return PostInfoDto.builder()
                .postId(post.getId())
                .userId(post.getUser().getId())
                .userName(post.getUser().getName())
                .dogName(post.getDogName())
                .imageUrl(post.getImageKey())
                .description(post.getDescription())
                .createdAt(post.getCreatedAt())
                .build();
    }
}
