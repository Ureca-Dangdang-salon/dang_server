package com.dangdangsalon.domain.contest.entity;

import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "contest_post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContestPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @Column(name = "image_key")
    private String imageKey;

    private String description;

    @Column(name = "like_count")
    private int likeCount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "contest_id", nullable = false)
    private Contest contest;

    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private GroomerProfile groomerProfile;

    @Builder
    public ContestPost(String imageKey, String description, int likeCount, LocalDateTime createdAt,
                       Contest contest, GroomerProfile groomerProfile) {
        this.imageKey = imageKey;
        this.description = description;
        this.likeCount = likeCount;
        this.createdAt = createdAt;
        this.contest = contest;
        this.groomerProfile = groomerProfile;
    }
}
