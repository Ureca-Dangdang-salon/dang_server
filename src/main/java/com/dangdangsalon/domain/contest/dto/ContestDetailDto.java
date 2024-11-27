package com.dangdangsalon.domain.contest.dto;


import com.dangdangsalon.domain.contest.entity.Contest;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContestDetailDto {

    private Long contestId;
    private String title;
    private LocalDateTime startedAt;
    private LocalDateTime endAt;
    private SimpleWinnerInfoDto recentWinner;

    public static ContestDetailDto create(Contest nowContest, SimpleWinnerInfoDto previousWinner) {
        return ContestDetailDto.builder()
                .contestId(nowContest.getId())
                .title(nowContest.getTitle())
                .startedAt(nowContest.getStartedAt())
                .endAt(nowContest.getEndAt())
                .recentWinner(previousWinner)
                .build();
    }
}
