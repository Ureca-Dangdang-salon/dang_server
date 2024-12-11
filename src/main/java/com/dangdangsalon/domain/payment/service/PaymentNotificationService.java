package com.dangdangsalon.domain.payment.service;

import com.dangdangsalon.domain.notification.service.NotificationService;
import com.dangdangsalon.domain.notification.service.RedisNotificationService;
import com.dangdangsalon.domain.orders.entity.Orders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentNotificationService {

    private final NotificationService notificationService;
    private final RedisNotificationService redisNotificationService;

    public void sendNotificationToUser(Orders orders) {

        Long userId = orders.getUser().getId();

        Optional<String> optionalFcmToken = notificationService.getFcmToken(userId);

        if (optionalFcmToken.isPresent()) {
            String fcmToken = optionalFcmToken.get();

            String title = "결제가 완료되었습니다";
            String body = "결제 내역을 확인해보세요.";

            if(notificationService.sendNotificationWithData(fcmToken, title, body, "결제", userId)){
                // redis 에 알림 내용 저장
                redisNotificationService.saveNotificationToRedis(userId, title, body, "결제", userId);
            }
        }
    }
}
