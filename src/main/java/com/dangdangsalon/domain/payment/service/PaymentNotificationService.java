package com.dangdangsalon.domain.payment.service;

import com.dangdangsalon.domain.notification.service.NotificationService;
import com.dangdangsalon.domain.notification.service.RedisNotificationService;
import com.dangdangsalon.domain.orders.entity.Orders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentNotificationService {

    private final NotificationService notificationService;
    private final RedisNotificationService redisNotificationService;

    @Async
    public void sendNotificationToUser(Orders orders) {

        Long userId = orders.getUser().getId();
        List<String> fcmTokens = notificationService.getFcmTokens(userId);

        String title = "결제가 완료되었습니다";
        String body = "결제 내역을 확인해보세요.";

        boolean isNotificationSent = false;

        for (String fcmToken : fcmTokens) {
            if (notificationService.sendNotificationWithData(fcmToken, title, body, "결제", userId)) {
                isNotificationSent = true;
            }
        }

        if (isNotificationSent) {
            redisNotificationService.saveNotificationToRedis(userId, title, body, "결제", userId);
        }
    }
}