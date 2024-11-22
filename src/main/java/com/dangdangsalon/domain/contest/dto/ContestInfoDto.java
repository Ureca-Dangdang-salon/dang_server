package com.dangdangsalon.domain.contest.dto;

import com.dangdangsalon.domain.contest.entity.Contest;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContestInfoDto {

    private Long contestId;
    private String title;
    private LocalDateTime startedAt;
    private LocalDateTime endAt;

    public static ContestInfoDto fromEntity(Contest contest) {
        return ContestInfoDto.builder()
                .contestId(contest.getId())
                .title(contest.getTitle())
                .startedAt(contest.getStartedAt())
                .endAt(contest.getEndAt())
                .build();
    }
}
