package com.dangdangsalon.domain.groomerprofile.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class ReviewInsertRequestDto {
    private String text;
    private double starScore;
    private List<String> imageKey;
}
