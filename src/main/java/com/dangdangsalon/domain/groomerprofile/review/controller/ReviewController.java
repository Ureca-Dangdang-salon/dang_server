package com.dangdangsalon.domain.groomerprofile.review.controller;

import com.dangdangsalon.domain.groomerprofile.review.dto.ReviewInsertRequestDto;
import com.dangdangsalon.domain.groomerprofile.review.dto.ReviewUpdateRequestDto;
import com.dangdangsalon.domain.groomerprofile.review.service.ReviewService;
import com.dangdangsalon.util.ApiUtil;
import com.dangdangsalon.util.ApiUtil.ApiSuccess;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;


    /*
    내가 쓴 리뷰 조회
    토큰으로 유저 ID 뽑아올 예정
     */
    @GetMapping("")
    public ApiSuccess<?> getUserReview(@RequestParam Long userId) {
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
    토큰으로 유저 ID 뽑아올 예정
     */
    @PostMapping("/{profileId}")
    public ApiSuccess<?> insertReview(@RequestParam Long userId, @PathVariable Long profileId,
                                              @RequestBody ReviewInsertRequestDto reviewInsertRequestDto) {
        reviewService.insertReview(userId, profileId, reviewInsertRequestDto);
        return ApiUtil.success("리뷰 등록이 완료되었습니다.");
    }

    /*
    리뷰 수정
    토큰으로 유저 ID 뽑아올 예정
     */
    @PutMapping("/{reviewId}")
    public ApiSuccess<?> updateReview(@RequestParam Long userId, @PathVariable Long reviewId,
                                      @RequestBody ReviewUpdateRequestDto reviewUpdateRequestDto) {
        reviewService.updateReview(userId, reviewId, reviewUpdateRequestDto);
        return ApiUtil.success("리뷰 수정이 완료되었습니다.");
    }

    /*
    리뷰 삭제
    토큰으로 유저 ID 뽑아올 예정
     */
    @DeleteMapping("/{reviewId}")
    public ApiSuccess<?> deleteReview(@RequestParam Long userId, @PathVariable Long reviewId) {
        reviewService.deleteReview(userId, reviewId);
        return ApiUtil.success("리뷰 삭제가 완료되었습니다.");
    }

}
