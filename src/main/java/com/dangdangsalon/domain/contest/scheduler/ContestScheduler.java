package com.dangdangsalon.domain.contest.scheduler;

import com.dangdangsalon.domain.contest.service.ContestService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContestScheduler {

    private final ContestService contestService;

    @Scheduled(cron = "0 0 0 1 * ?") //매월 1일 자정에 실행
    public void savePreviousContestWinner() {
        contestService.savePreviousContestWinner();
    }
}
