package com.dangdangsalon.domain.notification.service;

import com.dangdangsalon.domain.notification.dto.NotificationDto;
import com.dangdangsalon.domain.notification.dto.ReviewNotificationDto;
import com.dangdangsalon.domain.notification.entity.FcmToken;
import com.dangdangsalon.domain.notification.repository.FcmTokenRepository;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final FcmTokenRepository fcmTokenRepository;
    private final UserRepository userRepository;

    public void sendNotificationWithData(String token, String title, String body, String type, Long referenceId) {

        FcmToken fcmToken = fcmTokenRepository.findByFcmToken(token)
                .orElseThrow(() -> new IllegalArgumentException("FCM 토큰을 찾을 수 없습니다: " + token));

        User user = fcmToken.getUser();

        if (Boolean.FALSE.equals(user.getNotificationEnabled())) {
            log.info("알림 비활성화 상태로 알림 전송 건너뜀: " + user.getId());
            return;
        }

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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        Optional<FcmToken> existingToken = fcmTokenRepository.findByFcmToken(token);

        // 한 사람이 똑같은 디바이스로 여러 계정으로 로그인 시 기존 FCM 토큰 삭제 후 저장 로직(한 사람이 여러 계정 사용 가능)
        if (existingToken.isPresent()) {
            FcmToken tokenToUpdate = existingToken.get();
            if (!tokenToUpdate.getUser().getId().equals(userId)) {
                // 다른 사용자와 연결된 경우 삭제
                fcmTokenRepository.delete(tokenToUpdate);
            } else {
                // 동일한 사용자와 연결된 경우 갱신
                tokenToUpdate.updateTokenLastUserAt();
                return;
            }
        }

        // 새로운 토큰 생성 및 저장
        FcmToken newToken = FcmToken.builder()
                .fcmToken(token)
                .user(user)
                .lastUserAt(LocalDateTime.now())
                .build();
        fcmTokenRepository.save(newToken);
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

        // DTO 생성
        NotificationDto notificationDto = NotificationDto.builder()
                .id(notificationId)
                .title(title)
                .body(body)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .type(type)
                .referenceId(referenceId)
                .build();

        try {
            // DTO를 JSON 문자열로 변환 후 Redis List에 추가
            redisTemplate.opsForList().leftPush(key, objectMapper.writeValueAsString(notificationDto));

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

    public List<NotificationDto> getNotificationList(Long userId) {
        String key = "notifications:" + userId;

        List<String> notificationList = redisTemplate.opsForList().range(key, 0, -1);

        if (notificationList == null || notificationList.isEmpty()) {
            return Collections.emptyList();
        }

        return notificationList.stream()
                .map(notification -> {
                    try {
                        // JSON 문자열을 DTO로 변환
                        return objectMapper.readValue(notification, NotificationDto.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("알림 파싱 실패", e);
                    }
                })
                .filter(notificationDto -> !notificationDto.isRead())
                .toList();
    }

    public void updateNotificationAsRead(Long userId, String uuid) {
        String key = "notifications:" + userId;

        List<String> notifications = redisTemplate.opsForList().range(key, 0, -1);

        if (notifications == null || notifications.isEmpty()) {
            return;
        }

        try {
            for (int i = 0; i < notifications.size(); i++) {
                String notification = notifications.get(i);

                NotificationDto notificationDto = objectMapper.readValue(notification, NotificationDto.class);

                if (uuid.equals(notificationDto.getId()) && !notificationDto.isRead()) {
                    notificationDto.updateIsRead(true);

                    // 업데이트된 DTO를 JSON으로 변환하여 Redis에 저장
                    redisTemplate.opsForList().set(key, i, objectMapper.writeValueAsString(notificationDto));

                    // 읽지 않은 알림 개수 감소
                    redisTemplate.opsForValue().decrement("unread_count:" + userId);
                    break;
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("알림 읽음 처리 중 오류가 발생했습니다", e);
        }
    }

    public void notificationsAsRead(Long userId) {
        String key = "notifications:" + userId;

        List<String> notifications = redisTemplate.opsForList().range(key, 0, -1);

        if (notifications == null || notifications.isEmpty()) {
            return;
        }

        try {
            for (int i = 0; i < notifications.size(); i++) {
                String notification = notifications.get(i);

                NotificationDto notificationDto = objectMapper.readValue(notification, NotificationDto.class);

                if (!notificationDto.isRead()) {
                    notificationDto.updateIsRead(true);

                    redisTemplate.opsForList().set(key, i, objectMapper.writeValueAsString(notificationDto));
                }
            }

            // 읽지 않은 알림 개수를 0으로 갱신
            redisTemplate.opsForValue().set("unread_count:" + userId, "0");

        } catch (JsonProcessingException e) {
            throw new RuntimeException("모든 알림 읽음 처리 중 오류가 발생했습니다.", e);
        }
    }


    public void scheduleReviewNotification(Long userId, Long estimateId) {
        String key = "review_notification:" + estimateId;

        ReviewNotificationDto reminderData = ReviewNotificationDto.builder()
                .userId(userId)
                .estimateId(estimateId)
                .scheduledTime(LocalDateTime.now().plusMinutes(30).toString()) // 미용사가 미용완료 누르고 30분 뒤
                .build();

        try {
            redisTemplate.opsForValue().set(key, new ObjectMapper().writeValueAsString(reminderData));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("알림 예약 데이터 저장 중 오류 발생", e);
        }
    }
}
