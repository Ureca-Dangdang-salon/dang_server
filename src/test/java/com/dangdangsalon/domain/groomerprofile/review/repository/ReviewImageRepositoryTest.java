package com.dangdangsalon.domain.groomerprofile.review.repository;

import com.dangdangsalon.domain.groomerprofile.review.entity.Review;
import com.dangdangsalon.domain.groomerprofile.review.entity.ReviewImage;
import com.dangdangsalon.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ReviewImageRepositoryTest {

    @Autowired
    private ReviewImageRepository reviewImageRepository;

    @Autowired
    private EntityManager em;

    // 예약어 떄문에 User 엔티티에서 table 명을 "test_user" 등으로 변경해줘야 함
    @Test
    @DisplayName("리뷰 ID로 리뷰 이미지 삭제 테스트")
    void testDeleteByReviewId() {
        // User 생성 및 저장
        User user = User.builder()
                .name("user1")
                .build();
        em.persist(user);

        // Review 생성 및 저장
        Review review = Review.builder()
                .text("내용")
                .starScore(4)
                .user(user)
                .reviewImages(new ArrayList<>())
                .build();
        em.persist(review);

        // ReviewImage 생성 및 저장
        ReviewImage reviewImage1 = ReviewImage.builder()
                .imageKey("imageKey1")
                .review(review)
                .build();
        ReviewImage reviewImage2 = ReviewImage.builder()
                .imageKey("imageKey2")
                .review(review)
                .build();

        review.getReviewImages().add(reviewImage1);
        review.getReviewImages().add(reviewImage2);

        em.persist(reviewImage1);
        em.persist(reviewImage2);

        // 삭제 실행
        reviewImageRepository.deleteByReviewId(review.getId());

        // 삭제 후 검증
        List<ReviewImage> remainingImages = em.createQuery(
                        "SELECT ri FROM ReviewImage ri WHERE ri.review.id = :reviewId", ReviewImage.class)
                .setParameter("reviewId", review.getId())
                .getResultList();
        System.out.println(remainingImages.toString());
        assertThat(remainingImages).isEmpty(); // 남은 이미지가 없는지 확인
    }
}