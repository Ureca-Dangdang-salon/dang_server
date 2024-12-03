package com.dangdangsalon.domain.notification.service;

import com.dangdangsalon.domain.notification.entity.FcmToken;
import com.dangdangsalon.domain.notification.repository.FcmTokenRepository;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final FcmTokenRepository fcmTokenRepository;
    private final UserRepository userRepository;

    public void sendNotificationWithData(String token, String title, String body, String type, Long referenceId) {
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

        try {
            // 메시지 전송
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM 알림 전송 성공: " + response);

        } catch (FirebaseMessagingException e) {
            if (e.getMessagingErrorCode().equals(MessagingErrorCode.INVALID_ARGUMENT)) {
                log.error("FCM 토큰이 유효하지 않습니다.", e);
                deleteFcmToken(token);
            } else if (e.getMessagingErrorCode().equals(MessagingErrorCode.UNREGISTERED)) {
                log.error("FCM 토큰이 재발급 이전 토큰입니다.", e);
                deleteFcmToken(token);
            }
            else {
                throw new RuntimeException(e);
            }
        }
    }

    @Transactional
    public void saveOrUpdateFcmToken(Long userId, String token) {

        User user = userRepository.findById(userId).orElseThrow(() ->
                new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId)
        );

        Optional<FcmToken> existingToken = fcmTokenRepository.findByFcmToken(token);

        // 프론트에서 넘겨주는 토큰이 동일하면 시간만 업데이트
        if (existingToken.isPresent()) {
            existingToken.get().updateTokenLastUserAt();
        } else {
            // 다르면 저장 (한 사람이 여러 디바이스를 사용할 수도 있다)
            FcmToken newToken = FcmToken.builder()
                    .fcmToken(token)
                    .lastUserAt(LocalDateTime.now())
                    .user(user)
                    .build();

            fcmTokenRepository.save(newToken);
        }
    }

    public Optional<String> getFcmToken(Long userId) {
        return fcmTokenRepository.findByUserId(userId)
                .map(FcmToken::getFcmToken);
    }

    public void deleteFcmToken(String token) {
        fcmTokenRepository.deleteByFcmToken(token);
    }

    /**
     * 비활성 토큰 삭제 (60일 이상 업데이트되지 않은 경우)
     */
    @Transactional
    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정 실행
    public void removeInactiveTokens() {
        List<FcmToken> inactiveTokens = fcmTokenRepository.findAll().stream()
                .filter(token -> Duration.between(token.getLastUserAt(), LocalDateTime.now()).toDays() > 60)
                .toList();

        fcmTokenRepository.deleteAll(inactiveTokens);
    }

    @Transactional
    public void updateUserNotification(Long userId, boolean isEnabled) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId)
        );

        user.updateNotificationEnabled(isEnabled);
    }

    public void saveNotificationToRedis(Long userId, String title, String body, String type, Long referenceId) {
        String key = "notifications:" + userId;

        String notificationId = UUID.randomUUID().toString();

        // 알림 데이터 생성
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("id", notificationId);
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

        } catch (JsonProcessingException e) {
            log.error("Redis 알림 저장에 실패하였습니다", e);
        }
    }

    public Long getUnreadNotificationCount(Long userId) {
        String key = "unread_count:" + userId;
        String count = redisTemplate.opsForValue().get(key);
        if (count == null) {
            return 0L;
        } else {
            return Long.parseLong(count);
        }
    }

    public List<Map<String, Object>> getNotificationList(Long userId) {
        String key = "notifications:" + userId;

        // Redis List에서 알림 데이터 가져오기
        List<String> notificationList = redisTemplate.opsForList().range(key, 0, -1);

        // 알림이 없으면 빈 리스트 반환
        if (notificationList == null || notificationList.isEmpty()) {
            return Collections.emptyList();
        }

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

    public void updateNotificationAsRead(Long userId, String uuid) {
        String key = "notifications:" + userId;

        List<String> notifications = redisTemplate.opsForList().range(key, 0, -1);

        try {
            for (int i = 0; i < notifications.size(); i++) {
                String notification = notifications.get(i);
                Map<String, Object> notificationData = objectMapper.readValue(notification, new TypeReference<Map<String, Object>>() {});

                if (uuid.equals(notificationData.get("id"))) {
                    if (Boolean.FALSE.equals(notificationData.get("isRead"))) {
                        notificationData.put("isRead", true);

                        redisTemplate.opsForList().set(key, i, objectMapper.writeValueAsString(notificationData));

                        redisTemplate.opsForValue().decrement("unread_count:" + userId);
                    }
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("알림 읽음 처리 중 오류가 발생했습니다", e);
        }
    }

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

                Map<String, Object> notificationData = objectMapper.readValue(notification, new TypeReference<Map<String, Object>>() {});

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
}
