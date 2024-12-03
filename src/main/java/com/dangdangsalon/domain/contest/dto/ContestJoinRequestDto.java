package com.dangdangsalon.domain.contest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContestJoinRequestDto {

    private Long contestId;

    @JsonProperty("groomer_profile_id")
    private Long groomerProfileId;

    @JsonProperty("dog_name")
    private String dogName;

    @JsonProperty("image_url")
    private String imageUrl;

    private String description;
}
