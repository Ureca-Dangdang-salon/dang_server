package com.dangdangsalon.domain.estimate.service;

import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.entity.EstimateStatus;
import com.dangdangsalon.domain.estimate.repository.EstimateRepository;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.notification.service.NotificationService;
import com.dangdangsalon.domain.notification.service.RedisNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EstimateNotificationService {

    private final NotificationService notificationService;
    private final EstimateRepository estimateRepository;
    private final RedisNotificationService redisNotificationService;

    @Transactional
    public void sendNotificationToUser(EstimateRequest estimateRequest, Estimate estimate, GroomerProfile groomerProfile) {

        Long userId = estimateRequest.getUser().getId();

        Optional<String> optionalFcmToken = notificationService.getFcmToken(userId);

        if (optionalFcmToken.isPresent()) {
            String fcmToken = optionalFcmToken.get();

            String title = groomerProfile.getName() + "님이 견적을 보냈습니다.";
            String body = "견적 내용을 확인해보세요.";
            if(notificationService.sendNotificationWithData(fcmToken, title, body, "견적서", estimate.getId())){
                // Redis 에 알림 내용 저장
                redisNotificationService.saveNotificationToRedis(userId, title, body, "견적서", estimate.getId());
            };
        }
    }

    @Transactional
    public void updateEstimateStatus(Long estimateId){

        Estimate estimate = estimateRepository.findById(estimateId)
                .orElseThrow(() -> new IllegalArgumentException("견적서를 찾을 수 없습니다: " + estimateId));

        estimate.updateStatus(EstimateStatus.ACCEPTED);

        if (estimate.getStatus() == EstimateStatus.ACCEPTED) {
            Long userId = estimate.getEstimateRequest().getUser().getId();
            notificationService.scheduleReviewNotification(userId, estimateId);
        }
    }
}
