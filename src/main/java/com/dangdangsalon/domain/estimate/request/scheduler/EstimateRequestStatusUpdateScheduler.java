package com.dangdangsalon.domain.estimate.request.scheduler;

import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.estimate.request.entity.RequestStatus;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@EnableScheduling
public class EstimateRequestStatusUpdateScheduler {

    private final EstimateRequestRepository estimateRequestRepository;

    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정에 실행
    @Transactional
    public void updateExpiredEstimateRequestStatus() {

        List<EstimateRequest> expiredRequests = estimateRequestRepository.findAllByRequestDateBeforeAndRequestStatusNotIn(
                LocalDateTime.now(), List.of(RequestStatus.PAID, RequestStatus.CANCEL));

        expiredRequests.forEach(request -> request.updateRequestStatus(RequestStatus.CANCEL));
        estimateRequestRepository.saveAll(expiredRequests);
    }
}