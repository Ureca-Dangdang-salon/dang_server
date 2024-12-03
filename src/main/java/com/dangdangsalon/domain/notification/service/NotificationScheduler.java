package com.dangdangsalon.domain.notification.service;

import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.repository.EstimateRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
                    // JSON 문자열을 Map으로 변환
                    Map<String, Object> notificationData = objectMapper.readValue(notificationJson, new TypeReference<Map<String, Object>>() {});

                    // createdAt 확인
                    LocalDateTime createdAt = LocalDateTime.parse(notificationData.get("createdAt").toString());
                    if (Duration.between(createdAt, LocalDateTime.now()).toDays() > 14) {
                        // 만료된 알림 삭제
                        redisTemplate.opsForList().remove(key, 1, notificationJson);
                    }
                } catch (JsonProcessingException e) {
                    log.error("알림 데이터를 처리하는 중 오류 발생", e);
                }
            }
        }
    }
}
