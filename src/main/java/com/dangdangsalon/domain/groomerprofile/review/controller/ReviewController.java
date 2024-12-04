package com.dangdangsalon.domain.groomerprofile.review.controller;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.groomerprofile.review.dto.ReviewInsertRequestDto;
import com.dangdangsalon.domain.groomerprofile.review.dto.ReviewUpdateRequestDto;
import com.dangdangsalon.domain.groomerprofile.review.service.ReviewService;
import com.dangdangsalon.util.ApiUtil;
import com.dangdangsalon.util.ApiUtil.ApiSuccess;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    /*
    내가 쓴 리뷰 조회
     */
    @GetMapping("")
    public ApiSuccess<?> getUserReview(@AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();
        return ApiUtil.success(reviewService.getUserReviews(userId));
    }

    /*
    미용사에게 쓴 리뷰 조회
     */
    @GetMapping("/{profileId}")
    public ApiSuccess<?> getGroomerReview(@PathVariable Long profileId) {
        return ApiUtil.success(reviewService.getGroomerReviews(profileId));
    }

    /*
    리뷰 등록
     */
    @PostMapping("/{profileId}")
    public ApiSuccess<?> insertReview(@AuthenticationPrincipal CustomOAuth2User user, @PathVariable Long profileId,
                                              @RequestBody ReviewInsertRequestDto reviewInsertRequestDto) {
        Long userId = user.getUserId();

        reviewService.insertReview(userId, profileId, reviewInsertRequestDto);
        return ApiUtil.success("리뷰 등록이 완료되었습니다.");
    }

    /*
    리뷰 수정
     */
    @PutMapping("/{reviewId}")
    public ApiSuccess<?> updateReview(@AuthenticationPrincipal CustomOAuth2User user, @PathVariable Long reviewId,
                                      @RequestBody ReviewUpdateRequestDto reviewUpdateRequestDto) {
        Long userId = user.getUserId();

        reviewService.updateReview(userId, reviewId, reviewUpdateRequestDto);
        return ApiUtil.success("리뷰 수정이 완료되었습니다.");
    }

    /*
    리뷰 삭제
     */
    @DeleteMapping("/{reviewId}")
    public ApiSuccess<?> deleteReview(@AuthenticationPrincipal CustomOAuth2User user, @PathVariable Long reviewId) {
        Long userId = user.getUserId();

        reviewService.deleteReview(userId, reviewId);
        return ApiUtil.success("리뷰 삭제가 완료되었습니다.");
    }

}
