package com.dangdangsalon.domain.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final Duration NOTIFICATION_TTL = Duration.ofDays(14); // 14일

    /**
     * FCM 알림 전송
     */
    public void sendNotificationWithData(String token, String title, String body, String type, Long referenceId) {
        try {
            // 메시지 구성
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)  // 알림 제목
                            .setBody(body)    // 알림 내용
                            .build())
                    .putData("type", type)
                    .putData("referenceId", String.valueOf(referenceId))
                    .build();

            // 메시지 전송
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM 알림 전송 성공 " + response);

        } catch (FirebaseMessagingException e) {
            log.error("FCM 알림 전송에 실패했습니다. ", e);
        }
    }

    /**
     * FCM 토큰 저장
     */
    public void saveOrUpdateFcmToken(Long userId, String token) {
        String key = getRedisKey(userId);
        String existingToken = redisTemplate.opsForValue().get(key);

        // 새 토큰인지 확인
        if (existingToken == null || !existingToken.equals(token)) {
            // 새 토큰 저장 및 시간 업데이트
            redisTemplate.opsForValue().set(key, token);
            redisTemplate.opsForHash().put(key + ":timestamp", "updated_at", LocalDateTime.now().toString());
        }
    }

    /**
     * FCM 토큰 가져오기
     */
    public String getFcmToken(Long userId) {
        String key = getRedisKey(userId);
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * FCM 토큰 삭제
     */
    public void deleteFcmToken(Long userId) {
        String key = getRedisKey(userId);
        redisTemplate.delete(key);
        redisTemplate.delete(key + ":timestamp");
    }


    /**
     * 비활성 토큰 삭제 (30일 이상 업데이트되지 않은 경우)
     */
    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정 실행
    public void removeInactiveTokens() {
        Set<String> keys = redisTemplate.keys("fcm:token:*");
        for (String key : keys) {
            String lastUpdatedAt = (String) redisTemplate.opsForHash().get(key + ":timestamp", "updated_at");
            if (lastUpdatedAt != null) {
                LocalDateTime lastUpdated = LocalDateTime.parse(lastUpdatedAt);
                if (Duration.between(lastUpdated, LocalDateTime.now()).toDays() > 30) {
                    redisTemplate.delete(key);
                    redisTemplate.delete(key + ":timestamp");
                }
            }
        }
    }

    /**
     * 알림 저장 (Redis에 저장)
     */
    public void saveNotificationToRedis(Long userId, String title, String body, String type, Long referenceId) {
        String key = "notifications:" + userId;

        // 알림 데이터 생성
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("title", title);
        notificationData.put("body", body);
        notificationData.put("isRead", false);
        notificationData.put("createdAt", LocalDateTime.now());
        notificationData.put("type", type);
        notificationData.put("referenceId", referenceId);

        try {
            // 알림 데이터를 Redis List에 추가
            redisTemplate.opsForList().leftPush(key, objectMapper.writeValueAsString(notificationData));

            // 읽지 않은 알림 개수 증가
            redisTemplate.opsForValue().increment("unread_count:" + userId);

            // TTL 설정
            redisTemplate.expire(key, NOTIFICATION_TTL);
        } catch (JsonProcessingException e) {
            log.error("Redis 알림 저장에 실패하였습니다", e);
        }
    }

    /**
     * 읽지 않은 알림 개수 가져오기
     */
    public Long getUnreadNotificationCount(Long userId) {
        String key = "unread_count:" + userId;
        String count = redisTemplate.opsForValue().get(key);
        if (count == null) {
            return 0L;
        } else {
            return Long.parseLong(count);
        }
    }

    /**
     * 읽지 않은 알림 리스트 가져오기
     */
    public List<Map<String, Object>> getNotificationList(Long userId) {
        String key = "notifications:" + userId;

        // Redis List에서 알림 데이터 가져오기
        List<String> notificationList = redisTemplate.opsForList().range(key, 0, -1);

        // 알림이 없으면 빈 리스트 반환
        if (notificationList == null || notificationList.isEmpty()) {
            return Collections.emptyList();
        }

        // JSON 데이터를 Map으로 변환하고 읽지 않은 알림만 필터링
        return notificationList.stream()
                .map(notification -> {
                    try {
                        // JSON 문자열을 Map으로 변환
                        return objectMapper.readValue(notification, new TypeReference<Map<String, Object>>() {});
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("알림 파싱 실패", e);
                    }
                })
                .filter(notificationData -> Boolean.FALSE.equals(notificationData.get("isRead")))
                .toList();
    }


    /**
     * 알림 읽음 처리
     */
    public void updateNotificationAsRead(Long userId, int index) {
        String key = "notifications:" + userId;

        // 해당 알림 가져오기
        String notification = redisTemplate.opsForList().index(key, index);
        if (notification == null) {
            throw new IllegalArgumentException("알림을 찾을 수 없습니다: Index = " + index);
        }

        try {
            // JSON 데이터를 Map 으로 변환 후 읽음 처리
            Map<String, Object> notificationData = objectMapper.readValue(notification, Map.class);
            if (Boolean.FALSE.equals(notificationData.get("isRead"))) {
                notificationData.put("isRead", true);
                redisTemplate.opsForList().set(key, index, objectMapper.writeValueAsString(notificationData));

                // 읽지 않은 알림 개수 감소
                redisTemplate.opsForValue().decrement("unread_count:" + userId);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("알림 읽음 처리 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 모든 알림 읽음 처리
     */
    public void notificationsAsRead(Long userId) {
        String key = "notifications:" + userId;

        // Redis List 에서 모든 알림 가져오기
        List<String> notifications = redisTemplate.opsForList().range(key, 0, -1);

        if (notifications == null || notifications.isEmpty()) {
            return;
        }

        try {
            // 모든 알림을 순회하며 읽음 처리
            for (int i = 0; i < notifications.size(); i++) {
                String notification = notifications.get(i);

                // JSON 데이터를 Map으로 변환
                Map<String, Object> notificationData = objectMapper.readValue(notification, Map.class);

                // 읽지 않은 알림만 읽음 처리
                if (Boolean.FALSE.equals(notificationData.get("isRead"))) {
                    notificationData.put("isRead", true);
                    redisTemplate.opsForList().set(key, i, objectMapper.writeValueAsString(notificationData));
                }
            }

            // 읽지 않은 알림 개수를 0으로 갱신
            redisTemplate.opsForValue().set("unread_count:" + userId, "0");

        } catch (JsonProcessingException e) {
            throw new RuntimeException("모든 알림 읽음 처리 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * Redis Key 생성
     */
    private String getRedisKey(Long userId) {
        return "fcm:token:" + userId;
    }
}
