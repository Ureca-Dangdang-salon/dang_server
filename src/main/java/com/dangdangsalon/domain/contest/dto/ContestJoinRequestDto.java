package com.dangdangsalon.domain.contest.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContestJoinRequestDto {
    private Long groomerProfileId;
    private String dogName;
    private String imageUrl;
    private String description;
}
