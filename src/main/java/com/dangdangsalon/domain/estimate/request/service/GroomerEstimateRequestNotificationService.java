package com.dangdangsalon.domain.estimate.request.service;

import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.notification.service.NotificationService;
import com.dangdangsalon.domain.notification.service.RedisNotificationService;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroomerEstimateRequestNotificationService {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final RedisNotificationService redisNotificationService;

    public void sendNotificationToGroomer(EstimateRequest estimateRequest, GroomerProfile groomerProfile) {

        Long userId = groomerProfile.getUser().getId();

        User user = userRepository.findById(userId).orElseThrow(() ->
                new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId)
        );

        if (Boolean.FALSE.equals(user.getNotificationEnabled())) {
            log.info("알림 비활성화: " + user.getId());
        }else{
            Optional<String> optionalFcmToken = notificationService.getFcmToken(userId);

            if (optionalFcmToken.isPresent()) {
                String fcmToken = optionalFcmToken.get();

                String title = "새로운 견적 요청";
                String body = "새로운 견적 요청이 도착했습니다. 확인하세요.";
                // 알림 전송
                notificationService.sendNotificationWithData(fcmToken, title, body, "견적 요청", estimateRequest.getId());
                // redis 에 알림 내용 저장
                redisNotificationService.saveNotificationToRedis(userId, title, body, "견적 요청", estimateRequest.getId());
            }
        }
    }
}
