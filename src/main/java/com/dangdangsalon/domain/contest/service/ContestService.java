package com.dangdangsalon.domain.contest.service;

import com.dangdangsalon.domain.contest.dto.ContestDetailDto;
import com.dangdangsalon.domain.contest.dto.ContestInfoDto;
import com.dangdangsalon.domain.contest.dto.ContestJoinRequestDto;
import com.dangdangsalon.domain.contest.dto.PostInfoDto;
import com.dangdangsalon.domain.contest.dto.SimpleWinnerInfoDto;
import com.dangdangsalon.domain.contest.entity.Contest;
import com.dangdangsalon.domain.contest.entity.ContestPost;
import com.dangdangsalon.domain.contest.repository.ContestPostLikeRepository;
import com.dangdangsalon.domain.contest.repository.ContestPostRepository;
import com.dangdangsalon.domain.contest.repository.ContestRepository;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.groomerprofile.repository.GroomerProfileRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContestService {

    private final ContestRepository contestRepository;

    @Transactional(readOnly = true)
    public ContestInfoDto getLatestContest() {
        Contest latestContest = contestRepository.findTopByOrderByStartedAtDesc()
                .orElseThrow(() -> new IllegalArgumentException("진행중인 콘테스트가 없습니다."));

        return ContestInfoDto.fromEntity(latestContest);
    }

    @Transactional(readOnly = true)
    public ContestDetailDto getContestDetails(Long contestId) {
        Contest nowContest = contestRepository.findById(contestId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 콘테스트가 없습니다. InputId: " + contestId));

        Contest previousContest = contestRepository.findPreviousContest()
                .orElseThrow(() -> new IllegalArgumentException("이전 콘테스트가 없습니다."));

        SimpleWinnerInfoDto previousWinnerDto = null;

        if (previousContest.getWinnerPost() != null) {
            previousWinnerDto = SimpleWinnerInfoDto.fromEntity(previousContest.getWinnerPost());
        }

        return ContestDetailDto.create(nowContest, previousWinnerDto);
    }
}
