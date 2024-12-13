package com.dangdangsalon.chatgpt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ImageRequestDto {
    private String model;
    private String prompt;
    private int n;
    private String size;
}
