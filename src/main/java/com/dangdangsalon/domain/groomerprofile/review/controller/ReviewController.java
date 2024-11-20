package com.dangdangsalon.domain.groomerprofile.review.controller;

import com.dangdangsalon.domain.groomerprofile.review.dto.ReviewInsertRequestDto;
import com.dangdangsalon.domain.groomerprofile.review.dto.ReviewUpdateRequestDto;
import com.dangdangsalon.domain.groomerprofile.review.service.ReviewService;
import com.dangdangsalon.util.ApiUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    // 내가 쓴 리뷰 조회
    @GetMapping("")
    public ApiUtil.ApiSuccess<?> getUserReview(@RequestParam Long userId) {
        return ApiUtil.success(reviewService.getUserReviews(userId));
    }
    // 미용사에게 쓴 리뷰 조회
    @GetMapping("/{profileId}")
    public ApiUtil.ApiSuccess<?> getGroomerReview(@PathVariable Long profileId) {
        return ApiUtil.success(reviewService.getGroomerReviews(profileId));
    }

    // 리뷰 등록
    @PostMapping("/{profileId}")
    public ApiUtil.ApiSuccess<?> insertReview(@RequestParam Long userId, @PathVariable Long profileId,
                                              @RequestBody ReviewInsertRequestDto reviewInsertRequestDto) {
        return ApiUtil.success(reviewService.insertReview(userId, profileId, reviewInsertRequestDto));
    }

    // 리뷰 수정
    @PutMapping("/{reviewId}")
    public ApiUtil.ApiSuccess<?> updateReview(@RequestParam Long userId, @PathVariable Long reviewId,
                                              @RequestBody ReviewUpdateRequestDto reviewUpdateRequestDto) {
        return ApiUtil.success(reviewService.updateReview(userId, reviewId, reviewUpdateRequestDto));
    }

    // 리뷰 삭제
    @DeleteMapping("/{reviewId}")
    public ApiUtil.ApiSuccess<?> deleteReview(@RequestParam Long userId, @PathVariable Long reviewId) {
        return ApiUtil.success(reviewService.deleteReview(userId, reviewId));
    }

}
