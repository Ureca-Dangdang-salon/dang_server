package com.dangdangsalon.domain.notification.service;

import com.dangdangsalon.domain.notification.dto.EventNotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class EventNotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "event-alerts", groupId = "event-alerts-group")

    public void consumeEventNotification(EventNotificationDto notification) {
        // 여러 FCM 토큰에 대해 알림 한꺼번에 처리
        if (notification.getFcmToken() != null && !notification.getFcmToken().isEmpty()) {
            notification.getFcmToken().forEach(token -> {
                try {
                    notificationService.sendNotificationWithData(
                            token,
                            notification.getTitle(),
                            notification.getMessage(),
                            "event",
                            notification.getReferenceId()
                    );
                } catch (Exception e) {
                    log.error("토큰 {} 알림 전송 실패: {}", token, e.getMessage());
                }
            });
            log.info("이벤트 알림을 {} 개의 토큰에 전송 완료", notification.getFcmToken().size());
        } else {
            log.warn("알림을 위한 FCM 토큰이 없습니다.");
        }
    }
}