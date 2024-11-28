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
public class ReviewUserResponseDto {
    private Long reviewId;
    private Long profileId;
    private String groomerName;
    private String groomerImageKey;
    private String text;
    private double starScore;
    private String address;
    private List<String> reviewImages;

    public static ReviewUserResponseDto fromEntity(Review review) {
        return ReviewUserResponseDto.builder()
                .reviewId(review.getId())
                .profileId(review.getGroomerProfile().getId())
                .groomerName(review.getGroomerProfile().getName())
                .groomerImageKey(review.getGroomerProfile().getName())
                .text(review.getText())
                .starScore(review.getStarScore())
                .address(review.getGroomerProfile().getDetails().getAddress())
                .reviewImages(review.getReviewImages().stream()
                        .map(ReviewImage::getImageKey)
                        .toList())
                .build();
    }
}
