package com.dangdangsalon.domain.groomerprofile.review.dto;

import lombok.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewInsertRequestDto {
    private String text;
    private double starScore;
    private List<String> imageKey;
}
