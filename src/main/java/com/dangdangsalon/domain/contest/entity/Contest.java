package com.dangdangsalon.domain.contest.entity;

import com.dangdangsalon.config.base.BaseEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "contest")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Contest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @OneToOne
    @JoinColumn(name = "winner_post_id", nullable = true)
    private ContestPost winnerPost;

    @OneToMany(mappedBy = "contest", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContestPost> contestPosts = new ArrayList<>();

    @Builder
    public Contest(String title, String description, LocalDateTime startedAt, LocalDateTime endAt,
                   ContestPost winnerPost,
                   List<ContestPost> contestPosts) {
        this.title = title;
        this.description = description;
        this.startedAt = startedAt;
        this.endAt = endAt;
        this.winnerPost = winnerPost;
        this.contestPosts = contestPosts;
    }

    public void updateWinner(ContestPost winnerPost) {
        this.winnerPost = winnerPost;
    }
}
