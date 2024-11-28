package com.dangdangsalon.domain.groomerprofile.review.entity;

import com.dangdangsalon.config.base.BaseEntity;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double starScore;

    @Column(columnDefinition = "TEXT")
    private String text;

    @OneToMany(mappedBy = "review", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewImage> reviewImages;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groomer_profile_id")
    private GroomerProfile groomerProfile;

    @Builder
    public Review(double starScore, String text, List<ReviewImage> reviewImages,
                  User user, GroomerProfile groomerProfile) {
        this.starScore = starScore;
        this.text = text;
        this.reviewImages = reviewImages;
        this.user = user;
        this.groomerProfile = groomerProfile;
    }

    public void updateReview(String text) {
        this.text = text;
    }

    public boolean isValidUser(Long userId) {
        return this.getUser().getId().equals(userId);
    }
}
