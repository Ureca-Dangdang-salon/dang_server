package com.dangdangsalon.domain.notification.service;

import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.repository.EstimateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;
    private final NotificationEmailService notificationEmailService;
    private final EstimateRepository estimateRepository;

    @Scheduled(cron = "0 0 20 * * ?")
    public void sendReservationReminder() {
        // 내일의 시작과 끝 시간 계산
        LocalDateTime tomorrowStart = LocalDateTime.now().plusDays(1).toLocalDate().atStartOfDay();
        LocalDateTime tomorrowEnd = tomorrowStart.plusDays(1).minusNanos(1);

        // 내일 예약이 있는 Estimate 조회
        List<Estimate> estimateList = estimateRepository.findReservationsForTomorrow(tomorrowStart, tomorrowEnd);

        // 알림 전송
        for (Estimate estimate : estimateList) {
            Long userId = estimate.getEstimateRequest().getUser().getId(); // 사용자 ID 가져오기
            String email = estimate.getEstimateRequest().getUser().getEmail(); // 이메일 가져오기
            String title = "예약일 알림";

            String timeOnly = estimate.getDate().format(DateTimeFormatter.ofPattern("HH:mm"));
            String body = "내일 " + timeOnly + "시에 강아지 미용이 예정되어 있습니다.";

            // 푸시 알림
            String fcmToken = notificationService.getFcmToken(userId);
            if (fcmToken != null) {
                notificationService.sendNotificationWithData(fcmToken, title, body, "RESERVATION_REMINDER", estimate.getId());
            }

            // 이메일 알림
            if (email != null) {
                notificationEmailService.sendEmail(email, title, body);
            }

            // Redis 알림 저장
            notificationService.saveNotificationToRedis(userId, title, body, "RESERVATION_REMINDER", estimate.getId());
        }
    }
}
