package com.dangdangsalon.domain.review.service;

import com.dangdangsalon.domain.groomerprofile.entity.GroomerDetails;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.groomerprofile.repository.GroomerProfileRepository;
import com.dangdangsalon.domain.groomerprofile.review.dto.ReviewGroomerResponseDto;
import com.dangdangsalon.domain.groomerprofile.review.dto.ReviewInsertRequestDto;
import com.dangdangsalon.domain.groomerprofile.review.dto.ReviewUpdateRequestDto;
import com.dangdangsalon.domain.groomerprofile.review.dto.ReviewUserResponseDto;
import com.dangdangsalon.domain.groomerprofile.review.entity.Review;
import com.dangdangsalon.domain.groomerprofile.review.repository.ReviewImageRepository;
import com.dangdangsalon.domain.groomerprofile.review.repository.ReviewRepository;
import com.dangdangsalon.domain.groomerprofile.review.service.ReviewService;
import com.dangdangsalon.domain.region.entity.City;
import com.dangdangsalon.domain.region.entity.District;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewImageRepository reviewImageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroomerProfileRepository groomerProfileRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    @DisplayName("내가 쓴 리뷰 조회 성공 테스트")
    void testGetUserReviewsSuccess() {

        // Mock 데이터 설정
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);

        GroomerDetails groomerDetails = mock(GroomerDetails.class);
        given(groomerDetails.getAddress()).willReturn("미용사 주소");

        GroomerProfile groomerProfile = mock(GroomerProfile.class);
        given(groomerProfile.getId()).willReturn(1L);
        given(groomerProfile.getDetails()).willReturn(groomerDetails); // GroomerProfile 의 ID 모킹

        Review review = mock(Review.class);
        given(review.getGroomerProfile()).willReturn(groomerProfile); // Review 에서 GroomerProfile 반환값 설정
        given(reviewRepository.findAllByUserIdWithImages(user.getId())).willReturn(Optional.of(List.of(review)));

        // When
        List<ReviewUserResponseDto> result = reviewService.getUserReviews(user.getId());

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("내가 쓴 리뷰 조회 실패 테스트 - 빈 리스트 반환")
    void testGetUserReviewsEmptyResult() {
        Long userId = 1L;

        // Mock 설정
        given(reviewRepository.findAllByUserIdWithImages(userId)).willReturn(Optional.empty());

        // When
        List<ReviewUserResponseDto> result = reviewService.getUserReviews(userId);

        // Then
        assertNotNull(result); // 반환된 리스트는 null이 아니어야 함
        assertEquals(0, result.size(), "리뷰가 없는 경우 빈 리스트를 반환");

        // Repository 호출 검증
        verify(reviewRepository, times(1)).findAllByUserIdWithImages(userId);
    }

    @Test
    @DisplayName("미용사에게 쓴 리뷰 조회 성공 테스트")
    void testGetGroomerReviewsSuccess() {
        Long profileId = 1L;

        City city = City.builder()
                .name("서울시")
                .build();
        District district = District.builder()
                .name("종로구")
                .city(city)
                .build();

        // Mock 데이터 설정
        User user = mock(User.class);
        given(user.getDistrict()).willReturn(district);

        Review review = mock(Review.class);
        given(review.getUser()).willReturn(user); // Review 에서 User 반환값 설정
        given(reviewRepository.findAllByGroomerProfileIdWithImages(profileId)).willReturn(Optional.of(List.of(review)));

        // When
        List<ReviewGroomerResponseDto> result = reviewService.getGroomerReviews(profileId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("미용사에게 쓴 리뷰 조회 실패 테스트 - 빈 리스트 반환")
    void testGetGroomerReviewsFail() {
        Long profileId = 1L;

        // Mock 데이터 설정
        given(reviewRepository.findAllByGroomerProfileIdWithImages(profileId)).willReturn(Optional.empty());

        // When
        List<ReviewGroomerResponseDto> result = reviewService.getGroomerReviews(profileId);

        // Then
        assertNotNull(result); // 반환된 리스트는 null이 아니어야 함
        assertEquals(0, result.size(), "리뷰가 없는 경우 빈 리스트를 반환");

        // Repository 호출 검증
        verify(reviewRepository, times(1)).findAllByGroomerProfileIdWithImages(profileId);
    }

    @Test
    @DisplayName("리뷰 등록 성공 테스트")
    void testInsertReviewSuccess() {
        Long userId = 1L;
        Long profileId = 1L;
        ReviewInsertRequestDto reviewInsertRequestDto =
                new ReviewInsertRequestDto("Great service", 5, List.of("imageKey1", "imageKey2"));

        // Mock 데이터 설정
        User user = mock(User.class);
        GroomerProfile groomerProfile = mock(GroomerProfile.class);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(groomerProfileRepository.findById(profileId)).willReturn(Optional.of(groomerProfile));

        // When
        reviewService.insertReview(userId, profileId, reviewInsertRequestDto);

        // Then
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(reviewImageRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("리뷰 등록 실패 테스트 - 유저 권한 없음")
    void testInsertReviewFail_UserNotFound() {
        Long userId = 999L;
        Long profileId = 1L;
        ReviewInsertRequestDto reviewInsertRequestDto =
                new ReviewInsertRequestDto("Great service", 5, List.of("imageKey1"));

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // When && Then
        assertThrows(IllegalArgumentException.class, () ->
                reviewService.insertReview(userId, profileId, reviewInsertRequestDto));
    }

    @Test
    @DisplayName("리뷰 수정 성공 테스트")
    void testUpdateReviewSuccess() {
        Long userId = 1L;
        Long reviewId = 1L;
        ReviewUpdateRequestDto reviewUpdateRequestDto =
                new ReviewUpdateRequestDto("Updated review", List.of("newImageKey"));
        // Mock 데이터 설정
        Review review = mock(Review.class);
        given(reviewRepository.findByIdWithImages(reviewId)).willReturn(Optional.of(review));
        given(review.isValidUser(userId)).willReturn(true);

        // 실제 호출
        reviewService.updateReview(userId, reviewId, reviewUpdateRequestDto);

        // 검증
        verify(review, times(1)).updateReview(anyString()); // Review 에 있는 메서드
        verify(reviewImageRepository, times(1)).deleteByReviewId(reviewId);
        verify(reviewImageRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("리뷰 수정 실패 테스트 - 유저 권한 없음")
    void testUpdateReviewFail_InvalidUser() {
        Long userId = 999L;
        Long reviewId = 1L;
        ReviewUpdateRequestDto reviewUpdateRequestDto =
                new ReviewUpdateRequestDto("Updated review", List.of("newImageKey"));
        // Mock 데이터 설정
        Review review = mock(Review.class);
        given(reviewRepository.findByIdWithImages(reviewId)).willReturn(Optional.of(review));
        given(review.isValidUser(userId)).willReturn(false);

        // When 및 Then
        assertThrows(IllegalArgumentException.class, () ->
                reviewService.updateReview(userId, reviewId, reviewUpdateRequestDto));
    }

    @Test
    @DisplayName("리뷰 삭제 성공 테스트")
    void testDeleteReviewSuccess() {
        Long userId = 1L;
        Long reviewId = 1L;

        // Mock 객체 설정
        Review review = mock(Review.class);
        User user = mock(User.class);

        given(reviewRepository.findByIdWithImages(reviewId)).willReturn(Optional.of(review));
        given(review.getUser()).willReturn(user);
        given(user.getId()).willReturn(userId);


        // When
        reviewService.deleteReview(userId, reviewId);

        // Then
        verify(reviewRepository, times(1)).delete(review);
    }

    @Test
    @DisplayName("리뷰 삭제 실패 테스트 - 유저 권한 없음")
    void testDeleteReviewFail_InvalidUser() {
        Long userId = 999L;  // 다른 유저 ID
        Long reviewId = 1L;

        // Mock 객체 설정
        Review review = mock(Review.class);
        User user = mock(User.class);
        given(reviewRepository.findByIdWithImages(reviewId)).willReturn(Optional.of(review));
        given(review.getUser()).willReturn(user);


        // 실제 호출 및 예외 검증
        assertThrows(IllegalArgumentException.class, () -> reviewService.deleteReview(userId, reviewId));
    }
}