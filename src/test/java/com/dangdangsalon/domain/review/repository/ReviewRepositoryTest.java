package com.dangdangsalon.domain.review.repository;

import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.groomerprofile.review.entity.Review;
import com.dangdangsalon.domain.groomerprofile.review.entity.ReviewImage;
import com.dangdangsalon.domain.groomerprofile.review.repository.ReviewImageRepository;
import com.dangdangsalon.domain.groomerprofile.review.repository.ReviewRepository;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private EntityManager em;

    // 예약어 떄문에 User 엔티티에서 table 명을 "test_user" 등으로 변경해줘야 함
    @Test
    @DisplayName("유저 ID로 리뷰 조회 테스트")
    void testGetReviewById() {
        User user = User.builder()
                .name("user1")
                .build();
        em.persist(user);

        // Review 객체 생성
        Review review = Review.builder()
                .text("내용")
                .starScore(3)
                .user(user)
                .reviewImages(new ArrayList<>())
                .build();
        em.persist(review);

        // ReviewImage 객체 생성
        ReviewImage reviewImage1 = ReviewImage.builder()
                .imageKey("imageKey1")
                .review(review)
                .build();
        // ReviewImage 객체 생성
        ReviewImage reviewImage2 = ReviewImage.builder()
                .imageKey("imageKey2")
                .review(review)
                .build();

        review.getReviewImages().add(reviewImage1);
        review.getReviewImages().add(reviewImage2);

        em.persist(reviewImage1);
        em.persist(reviewImage2);

        // When
        Optional<List<Review>> result = reviewRepository.findAllByUserIdWithImages(user.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().get(0).getUser().getId()).isEqualTo(user.getId());

        // 리뷰 이미지가 제대로 연결되었는지 확인
        assertThat(result.get().get(0).getReviewImages()).hasSize(2);
        assertThat(result.get().get(0).getReviewImages().get(0).getImageKey()).isEqualTo("imageKey1");
    }

    @Test
    @DisplayName("리뷰 ID로 리뷰와 이미지를 조회 테스트")
    void testFindByIdWithImages() {
        // Review 객체 생성
        Review review = Review.builder()
                .text("내용")
                .starScore(4)
                .reviewImages(new ArrayList<>())
                .build();
        em.persist(review);

        // ReviewImage 객체 생성
        ReviewImage reviewImage1 = ReviewImage.builder()
                .imageKey("imageKey1")
                .review(review)
                .build();
        ReviewImage reviewImage2 = ReviewImage.builder()
                .imageKey("imageKey2")
                .review(review)
                .build();

        // 리스트에 직접 추가
        review.getReviewImages().add(reviewImage1);
        review.getReviewImages().add(reviewImage2);

        em.persist(reviewImage1);
        em.persist(reviewImage2);

        // When
        Optional<Review> result = reviewRepository.findByIdWithImages(review.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(review.getId());
        assertThat(result.get().getReviewImages()).hasSize(2); // 이미지 2개 연결 확인

        // 리뷰 이미지 검증
        assertThat(result.get().getReviewImages().get(0).getImageKey()).isEqualTo("imageKey1");
        assertThat(result.get().getReviewImages().get(1).getImageKey()).isEqualTo("imageKey2");
    }

    @Test
    @DisplayName("GroomerProfile ID로 리뷰와 이미지를 조회 테스트")
    void testFindAllByGroomerProfileIdWithImages() {
        GroomerProfile profile = GroomerProfile.builder()
                .name("groomer1")
                .build();
        em.persist(profile);

        // Review 객체 생성
        Review review1 = Review.builder()
                .text("리뷰1")
                .starScore(5)
                .groomerProfile(profile)
                .reviewImages(new ArrayList<>())
                .build();
        em.persist(review1);

        Review review2 = Review.builder()
                .text("리뷰2")
                .starScore(3)
                .groomerProfile(profile)
                .reviewImages(new ArrayList<>())
                .build();
        em.persist(review2);

        // ReviewImage 객체 생성
        ReviewImage reviewImage1 = ReviewImage.builder()
                .imageKey("imageKey1")
                .review(review1)
                .build();
        ReviewImage reviewImage2 = ReviewImage.builder()
                .imageKey("imageKey2")
                .review(review2)
                .build();

        review1.getReviewImages().add(reviewImage1);
        review2.getReviewImages().add(reviewImage2);

        em.persist(reviewImage1);
        em.persist(reviewImage2);

        // When
        Optional<List<Review>> result = reviewRepository.findAllByGroomerProfileIdWithImages(profile.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(2); // 리뷰 2개 연결 확인

        // 리뷰 이미지 검증
        assertThat(result.get().get(0).getReviewImages()).hasSize(1);
        assertThat(result.get().get(0).getReviewImages().get(0).getImageKey()).isEqualTo("imageKey1");
        assertThat(result.get().get(1).getReviewImages()).hasSize(1);
        assertThat(result.get().get(1).getReviewImages().get(0).getImageKey()).isEqualTo("imageKey2");
    }

}