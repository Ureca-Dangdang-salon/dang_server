package com.dangdangsalon.domain.contest.service;

import com.dangdangsalon.domain.contest.dto.ContestInfoDto;
import com.dangdangsalon.domain.contest.entity.Contest;
import com.dangdangsalon.domain.contest.repository.ContestRepository;
import lombok.RequiredArgsConstructor;
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
}
