package com.dangdangsalon.domain.groomerprofile.review.repository;

import com.dangdangsalon.domain.groomerprofile.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.reviewImages WHERE r.user.id = :userId")
    Optional<List<Review>> findAllByUserIdWithImages(@Param("userId") Long userId);
    
    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.reviewImages WHERE r.id = :reviewId")
    Optional<Review> findByIdWithImages(@Param("reviewId") Long reviewId);

    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.reviewImages WHERE r.groomerProfile.id = :profileId")
    Optional<List<Review>> findAllByGroomerProfileIdWithImages(@Param("profileId") Long profileId);
}
