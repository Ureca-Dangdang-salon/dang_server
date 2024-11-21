package com.dangdangsalon.domain.contest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContestJoinRequestDto {

    @JsonProperty("groomer_profile_id")
    private Long groomerProfileId;

    private String dogName;
    private String imageUrl;
    private String description;
}
