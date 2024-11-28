package com.dangdangsalon.domain.groomerprofile.review.repository;

import com.dangdangsalon.domain.groomerprofile.review.entity.Review;
import com.dangdangsalon.domain.groomerprofile.review.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    void deleteByReviewId(Long reviewId);
}
