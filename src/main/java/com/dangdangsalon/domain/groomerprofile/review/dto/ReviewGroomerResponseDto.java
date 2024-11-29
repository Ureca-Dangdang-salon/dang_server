package com.dangdangsalon.domain.groomerprofile.review.dto;

import com.dangdangsalon.domain.groomerprofile.review.entity.Review;
import com.dangdangsalon.domain.groomerprofile.review.entity.ReviewImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class ReviewGroomerResponseDto {
    private Long reviewId;
    private String userName;
    private String userImageKey;
    private String text;
    private double starScore;
    private String city;
    private String district;
    private List<String> reviewImages;

    public static ReviewGroomerResponseDto fromEntity(Review review) {
        return ReviewGroomerResponseDto.builder()
                .reviewId(review.getId())
                .userName(review.getUser().getName())
                .userImageKey(review.getUser().getImageKey())
                .text(review.getText())
                .starScore(review.getStarScore())
                .city(review.getUser().getDistrict().getCity().getName())
                .district(review.getUser().getDistrict().getName())
                .reviewImages(review.getReviewImages().stream()
                        .map(ReviewImage::getImageKey)
                        .toList())
                .build();
    }

}
