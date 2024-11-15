package com.dangdangsalon.domain.groomerprofile.review.entity;

import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double starScore;

    @Column(columnDefinition = "TEXT")
    private String text;

    private String imageKey;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "groomer_profile_id")
    private GroomerProfile groomerProfile;

    @Builder
    public Review(double starScore, String text, String imageKey, User user, GroomerProfile groomerProfile) {
        this.starScore = starScore;
        this.text = text;
        this.imageKey = imageKey;
        this.user = user;
        this.groomerProfile = groomerProfile;
    }
}
