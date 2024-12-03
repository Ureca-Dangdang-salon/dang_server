package com.dangdangsalon.domain.payment.service;

import com.dangdangsalon.domain.notification.service.NotificationService;
import com.dangdangsalon.domain.notification.service.RedisNotificationService;
import com.dangdangsalon.domain.orders.entity.Orders;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentNotificationService {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final RedisNotificationService redisNotificationService;

    public void sendNotificationToUser(Orders orders) {

        Long userId = orders.getUser().getId();

        User user = userRepository.findById(userId).orElseThrow(() ->
                new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId)
        );
        if (Boolean.FALSE.equals(user.getNotificationEnabled())) {
            log.info("알림 비활성화: " + user.getId());
        } else {
            Optional<String> optionalFcmToken = notificationService.getFcmToken(userId);

            if (optionalFcmToken.isPresent()) {
                String fcmToken = optionalFcmToken.get();

                String title = "결제가 완료되었습니다";
                String body = "결제 내역을 확인해보세요.";
                notificationService.sendNotificationWithData(fcmToken, title, body, "결제", userId); // 알림 전송

                // redis 에 알림 내용 저장
                redisNotificationService.saveNotificationToRedis(userId, title, body, "결제", userId);
            }
        }
    }
}
