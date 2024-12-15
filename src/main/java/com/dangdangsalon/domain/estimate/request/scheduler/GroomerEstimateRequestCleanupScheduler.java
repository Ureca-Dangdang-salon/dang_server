package com.dangdangsalon.domain.estimate.request.scheduler;

import com.dangdangsalon.domain.groomerprofile.request.repository.GroomerEstimateRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@EnableScheduling
public class GroomerEstimateRequestCleanupScheduler {

    private final GroomerEstimateRequestRepository groomerEstimateRequestRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void cleanupOldEstimateRequests() {
        // 현재 시간 기준으로 지난 요청 삭제
        groomerEstimateRequestRepository.deleteByRequestDateBefore(LocalDateTime.now());
    }
}
