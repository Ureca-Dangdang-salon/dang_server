package com.dangdangsalon.chatgpt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GenerateImageAnalysisResponseDto {
    private String imageUrl;
    private String analysisResult;
    private String matchingCelebrity;
    private String celebrityImageUrl;
}
