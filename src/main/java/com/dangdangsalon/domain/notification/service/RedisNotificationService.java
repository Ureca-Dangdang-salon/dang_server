package com.dangdangsalon.domain.notification.service;

import com.dangdangsalon.domain.notification.dto.NotificationDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisNotificationService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

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

}
