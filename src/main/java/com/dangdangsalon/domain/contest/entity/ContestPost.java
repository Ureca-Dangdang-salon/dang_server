package com.dangdangsalon.domain.contest.entity;

import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "contest_post", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"contest_id", "user_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContestPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_key")
    private String imageKey;

    private String description;

    @Column(name = "like_count", columnDefinition = "INT DEFAULT 0")
    private int likeCount;

    @ManyToOne
    @JoinColumn(name = "contest_id", nullable = false)
    private Contest contest;

    @ManyToOne
    @JoinColumn(name = "groomer_profile_id", nullable = false)
    private GroomerProfile groomerProfile;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public ContestPost(String imageKey, String description, int likeCount, Contest contest,
                       GroomerProfile groomerProfile,
                       User user) {
        this.imageKey = imageKey;
        this.description = description;
        this.likeCount = likeCount;
        this.contest = contest;
        this.groomerProfile = groomerProfile;
        this.user = user;
    }
}
