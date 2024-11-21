package com.dangdangsalon.domain.contest.dto;

import com.dangdangsalon.domain.contest.entity.Contest;
import com.dangdangsalon.domain.contest.entity.ContestPost;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LastContestWinnerDto {

    private Long contestId;
    private Long grommerProfileId;
    private PostInfoDto post;

    public static LastContestWinnerDto fromEntity(Contest contest) {
        ContestPost winnerPost = contest.getWinnerPost();

        return LastContestWinnerDto.builder()
                .contestId(contest.getId())
                .grommerProfileId(winnerPost.getGroomerProfile().getId())
                .post(PostInfoDto.create(winnerPost, false))
                .build();
    }
}
