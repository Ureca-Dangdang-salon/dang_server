package com.dangdangsalon.domain.estimate.service;

import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.notification.service.NotificationService;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EstimateNotificationService {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public void sendNotificationToUser(EstimateRequest estimateRequest, Estimate estimate, GroomerProfile groomerProfile) {

        Long userId = estimateRequest.getUser().getId();

        User user = userRepository.findById(userId).orElseThrow(() ->
                new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId)
        );

        if (Boolean.FALSE.equals(user.getNotificationEnabled())) {
            log.info("알림 비활성화: " + user.getId());
        }else {
            String fcmToken = notificationService.getFcmToken(userId);
            String title = groomerProfile.getName() + "님이 견적을 보냈습니다.";
            String body = "견적 내용을 확인해보세요.";
            notificationService.sendNotificationWithData(fcmToken, title, body,"견적서", estimate.getId()); // 알림 전송

            // redis 에 알림 내용 저장
            notificationService.saveNotificationToRedis(userId, title, body, "견적서", estimate.getId());
        }
    }
}
