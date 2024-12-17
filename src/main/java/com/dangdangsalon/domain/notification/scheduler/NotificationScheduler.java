package com.dangdangsalon.domain.notification.scheduler;

import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.repository.EstimateRepository;
import com.dangdangsalon.domain.notification.dto.NotificationDto;
import com.dangdangsalon.domain.notification.dto.ReviewNotificationDto;
import com.dangdangsalon.domain.notification.service.NotificationEmailService;
import com.dangdangsalon.domain.notification.service.NotificationService;
import com.dangdangsalon.domain.notification.service.RedisNotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;
    private final NotificationEmailService notificationEmailService;
    private final ObjectMapper objectMapper;
    private final EstimateRepository estimateRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisNotificationService redisNotificationService;

    @Transactional
    @Scheduled(cron = "0 05 14 * * ?")
    public void sendReservationReminder() {

        log.info("예약 알림 스케줄러 시작됨: {}", LocalDateTime.now());

        LocalDateTime tomorrowStart = LocalDateTime.now().plusDays(1).toLocalDate().atStartOfDay();
        LocalDateTime tomorrowEnd = tomorrowStart.plusDays(1).minusNanos(1);

        List<Estimate> estimateList = estimateRepository.findReservationsForTomorrow(tomorrowStart, tomorrowEnd);

        for (Estimate estimate : estimateList) {
            Long userId = estimate.getEstimateRequest().getUser().getId();
            String email = estimate.getEstimateRequest().getUser().getEmail();
            String title = "예약일 알림";

            String timeOnly = estimate.getDate().format(DateTimeFormatter.ofPattern("HH:mm"));
            String body = "내일 " + timeOnly + "시에 강아지 미용이 예정되어 있습니다.";

            // 푸시 알림
            List<String> fcmTokens = notificationService.getFcmTokens(userId);
            for (String fcmToken : fcmTokens) {
                notificationService.sendNotificationWithData(fcmToken, title, body, "RESERVATION_REMINDER", estimate.getId());
            }

            // 이메일 알림
            if (email != null) {
                notificationEmailService.sendEmailWithTemplate(
                        email,
                        title,
                        "/templates/email.html",
                        Map.of(
                                "userName", estimate.getEstimateRequest().getUser().getName(),
                                "reservationDateTime", estimate.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                        )
                );
                log.info("이메일 알림 전송: 사용자 ID {}, 이메일 {}", userId, email);
            }

            // Redis 알림 저장
            redisNotificationService.saveNotificationToRedis(userId, title, body, "RESERVATION_REMINDER", estimate.getId());
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void removeOldNotifications() {
        Set<String> keys = redisTemplate.keys("notifications:*");

        for (String key : keys) {
            List<String> notificationList = redisTemplate.opsForList().range(key, 0, -1);

            if (notificationList == null || notificationList.isEmpty()) {
                continue;
            }

            for (String notificationJson : notificationList) {
                try {
                    NotificationDto notification = objectMapper.readValue(notificationJson, NotificationDto.class);

                    if (notification.getCreatedAt() != null &&
                            Duration.between(notification.getCreatedAt(), LocalDateTime.now()).toDays() > 14) {
                        redisTemplate.opsForList().remove(key, 1, notificationJson);
                    }
                } catch (JsonProcessingException e) {
                    log.error("알림 데이터를 처리하는 중 오류 발생: {}", notificationJson, e);
                }
            }
        }
    }

    @Transactional
    @Scheduled(cron = "0 * * * * ?")
    public void sendReviewReminders() {
        Set<String> keys = redisTemplate.keys("review_notification:*");

        if (keys != null) {
            for (String key : keys) {
                try {
                    String jsonData = redisTemplate.opsForValue().get(key);

                    ReviewNotificationDto reminderData = objectMapper.readValue(jsonData, ReviewNotificationDto.class);

                    LocalDateTime scheduledTime = LocalDateTime.parse(reminderData.getScheduledTime());

                    if (LocalDateTime.now().isAfter(scheduledTime)) {
                        Long userId = reminderData.getUserId();
                        Long estimateId = reminderData.getEstimateId();

                        Estimate estimate = estimateRepository.findWithEstimateById(estimateId)
                                .orElseThrow(() -> new IllegalArgumentException("견적서가 없습니다: " + estimateId));

                        String title = "리뷰 작성 요청";
                        String body = estimate.getGroomerProfile().getName() + "님에 대한 리뷰를 작성해주세요!";

                        List<String> fcmTokens = notificationService.getFcmTokens(userId);

                        boolean isNotificationSent = false;

                        for (String fcmToken : fcmTokens) {
                            if (notificationService.sendNotificationWithData(fcmToken, title, body, "REVIEW_REQUEST", estimateId)) {
                                isNotificationSent = true;
                            }
                        }

                        // Redis에 알림 데이터 저장 (한 번만 저장)
                        if (isNotificationSent) {
                            redisNotificationService.saveNotificationToRedis(userId, title, body, "REVIEW_REQUEST", estimateId);
                        }

                        // Redis에서 예약 데이터 삭제
                        redisTemplate.delete(key);
                    }
                } catch (Exception e) {
                    log.error("리뷰 작성 알림 전송 중 오류 발생: " + key, e);
                }
            }
        }
    }
}
