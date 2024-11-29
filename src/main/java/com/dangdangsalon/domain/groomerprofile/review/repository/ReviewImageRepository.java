package com.dangdangsalon.domain.groomerprofile.review.repository;


import com.dangdangsalon.domain.groomerprofile.review.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;


public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    @Modifying
    @Query("DELETE FROM ReviewImage ri WHERE ri.review.id = :reviewId")
    void deleteByReviewId(Long reviewId);
}
