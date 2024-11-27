package com.dangdangsalon.domain.groomerprofile.review.dto;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewUpdateRequestDto {
    private String text;
    private List<String> reviewImages;
}
