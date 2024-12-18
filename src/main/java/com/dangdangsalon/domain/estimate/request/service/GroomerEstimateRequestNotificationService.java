package com.dangdangsalon.domain.estimate.request.service;

import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.notification.service.NotificationService;
import com.dangdangsalon.domain.notification.service.RedisNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroomerEstimateRequestNotificationService {

    private final NotificationService notificationService;
    private final RedisNotificationService redisNotificationService;

    @Async
    public void sendNotificationToGroomer(EstimateRequest estimateRequest, GroomerProfile groomerProfile) {

        Long userId = groomerProfile.getUser().getId();
        List<String> fcmTokens = notificationService.getFcmTokens(userId);

        String title = "새로운 견적 요청";
        String body = "새로운 견적 요청이 도착했습니다. 확인하세요.";

        boolean isNotificationSent = false;

        for (String fcmToken : fcmTokens) {
            if (notificationService.sendNotificationWithData(fcmToken, title, body, "견적 요청", estimateRequest.getId())) {
                isNotificationSent = true;
            }
        }

        if (isNotificationSent) {
            redisNotificationService.saveNotificationToRedis(userId, title, body, "견적 요청", estimateRequest.getId());
        }
    }
}
