package com.dangdangsalon.domain.groomerprofile.review.entity;
import com.dangdangsalon.config.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "review_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewImage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "image_key")
    private String imageKey;
    @ManyToOne
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;
    @Builder
    public ReviewImage(String imageKey, Review review) {
        this.imageKey = imageKey;
        this.review = review;
    }
}