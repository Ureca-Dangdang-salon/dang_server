package com.dangdangsalon.domain.groomerprofile.review.service;

import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.groomerprofile.repository.GroomerProfileRepository;
import com.dangdangsalon.domain.groomerprofile.review.dto.ReviewInsertRequestDto;
import com.dangdangsalon.domain.groomerprofile.review.dto.ReviewUpdateRequestDto;
import com.dangdangsalon.domain.groomerprofile.review.dto.ReviewGroomerResponseDto;
import com.dangdangsalon.domain.groomerprofile.review.dto.ReviewUserResponseDto;
import com.dangdangsalon.domain.groomerprofile.review.entity.Review;
import com.dangdangsalon.domain.groomerprofile.review.entity.ReviewImage;
import com.dangdangsalon.domain.groomerprofile.review.repository.ReviewImageRepository;
import com.dangdangsalon.domain.groomerprofile.review.repository.ReviewRepository;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final UserRepository userRepository;
    private final GroomerProfileRepository groomerProfileRepository;

    /*
    내가 쓴 리뷰 조회
    토큰으로 유저 ID 뽑아올 예정
     */
    @Transactional(readOnly = true)
    public List<ReviewUserResponseDto> getUserReviews(Long userId) {

        List<Review> reviews = reviewRepository.findAllByUserIdWithImages(userId)
                .orElse(Collections.emptyList());

        // 리뷰 없으면 빈 리스트, 있으면 조회
        if (reviews.isEmpty()) {
            return Collections.emptyList();
        }
        return reviews.stream().map(ReviewUserResponseDto::fromEntity).toList();

    }

    /*
    미용사에게 쓴 리뷰 조회
    토큰으로 유저 ID 뽑아올 예정
     */
    @Transactional(readOnly = true)
    public List<ReviewGroomerResponseDto> getGroomerReviews(Long profileId) {

        List<Review> reviews = reviewRepository.findAllByGroomerProfileIdWithImages(profileId)
                .orElse(Collections.emptyList());

        // 리뷰 없으면 빈 리스트, 있으면 조회
        if (reviews.isEmpty()) {
            return Collections.emptyList();
        }
        return reviews.stream().map(ReviewGroomerResponseDto::fromEntity).toList();
    }

    @Transactional
    public String insertReview(Long userId, Long profileId, ReviewInsertRequestDto reviewInsertRequestDto) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new IllegalArgumentException("유저 아이디를 찾을 수 없습니다 : " + userId));

        GroomerProfile groomerProfile = groomerProfileRepository.findById(profileId).orElseThrow(() ->
                new IllegalArgumentException("프로필 아이디를 찾을 수 없습니다 : " + profileId));

        // 리뷰 생성
        Review review = Review.builder()
                .starScore(reviewInsertRequestDto.getStarScore())
                .text(reviewInsertRequestDto.getText())
                .user(user)
                .groomerProfile(groomerProfile)
                .build();
        reviewRepository.save(review);

        // 리뷰 이미지 있으면 저장
        if(!reviewInsertRequestDto.getImageKey().isEmpty()) {
            List<ReviewImage> reviewImages = reviewInsertRequestDto.getImageKey().stream()
                    .map(imageKey -> ReviewImage.builder()
                            .imageKey(imageKey)
                            .review(review)
                            .build())
                    .toList();
            reviewImageRepository.saveAll(reviewImages);
        }

        return "리뷰 등록이 완료되었습니다.";
    }

    @Transactional
    public String updateReview(Long userId, Long reviewId, ReviewUpdateRequestDto reviewUpdateRequestDto) {
        Review review = reviewRepository.findByIdWithImages(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다 : " + reviewId));


        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 리뷰를 수정할 권한이 없습니다.");
        }

        // 내용 수정
        review.updateReview(reviewUpdateRequestDto.getText());

        if (!reviewUpdateRequestDto.getReviewImages().isEmpty()) {
            // 기존 이미지 삭제
            reviewImageRepository.deleteByReviewId(reviewId);

            // 새로운 이미지 저장
            List<ReviewImage> reviewImages = reviewUpdateRequestDto.getReviewImages().stream()
                    .map(imageKey -> ReviewImage.builder()
                            .imageKey(imageKey)
                            .review(review)
                            .build())
                    .toList();
            reviewImageRepository.saveAll(reviewImages);
        }else{
            reviewImageRepository.deleteByReviewId(reviewId);
        }

        return "리뷰 수정이 완료되었습니다.";
    }

    @Transactional
    public String deleteReview(Long userId, Long reviewId) {

        Review review = reviewRepository.findByIdWithImages(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다 : " + reviewId));

        // 어드민도 적용 할 예정
        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 리뷰를 삭제할 권한이 없습니다.");
        }
        reviewRepository.delete(review);

        return "리뷰 삭제가 완료되었습니다.";
    }
}
