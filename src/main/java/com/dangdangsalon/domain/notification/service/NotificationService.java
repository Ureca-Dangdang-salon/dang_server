package com.dangdangsalon.domain.notification.service;

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
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final RedisTemplate<String, String> redisTemplate;
    private final FcmTokenRepository fcmTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public boolean sendNotificationWithData(String token, String title, String body, String type, Long referenceId) {

        FcmToken fcmToken = fcmTokenRepository.findByFcmToken(token)
                .orElseThrow(() -> new IllegalArgumentException("FCM 토큰을 찾을 수 없습니다: " + token));

        User user = fcmToken.getUser();

        if (Boolean.FALSE.equals(user.getNotificationEnabled())) {
            log.info("알림 비활성화 상태로 알림 전송 건너뜀: " + user.getId());
            return false; // 알림 비활성화 시 false 반환
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
            return true;

        } catch (FirebaseMessagingException e) {
            // 오류에 따라 FCM 토큰 삭제 처리
            if (e.getMessagingErrorCode().equals(MessagingErrorCode.INVALID_ARGUMENT)) {
                log.error("FCM 토큰이 유효하지 않습니다.", e);
                deleteFcmToken(token);
            } else if (e.getMessagingErrorCode().equals(MessagingErrorCode.UNREGISTERED)) {
                log.error("FCM 토큰이 재발급 이전 토큰입니다.", e);
                deleteFcmToken(token);
            } else {
                log.error("알림 전송 중 오류 발생", e);
            }
            return false;
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
