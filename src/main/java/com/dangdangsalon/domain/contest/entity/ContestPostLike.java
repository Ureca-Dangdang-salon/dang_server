package com.dangdangsalon.domain.contest.entity;

import com.dangdangsalon.config.base.BaseEntity;
import com.dangdangsalon.domain.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_like", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "contest_post_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContestPostLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_post_id", nullable = false)
    private ContestPost contestPost;

    @Builder
    public ContestPostLike(User user, ContestPost contestPost) {
        this.user = user;
        this.contestPost = contestPost;
    }
}