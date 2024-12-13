package com.dangdangsalon.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationTopicService {

    // 주제로 메시지 보내기
    public void sendNotificationToTopic(String topic, String title, String body) {

        Message message = Message.builder()
                .setTopic(topic)
                .putData("title", title)  // 알림 제목
                .putData("body", body)    // 알림 내용
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("주제 '{}'로 메시지 전송 성공: {}", topic, response);
        } catch (FirebaseMessagingException e) {
            log.error("주제 '{}'로 메시지 전송 실패: {}", topic, e.getMessage(), e);
            throw new RuntimeException("메시지 전송 실패", e);
        }
    }

    /**
     * 특정 주제에 FCM 토큰을 구독
     */
    public void subscribeToTopic(String fcmToken, String topic) {
        try {
            FirebaseMessaging.getInstance().subscribeToTopic(List.of(fcmToken), topic);
            log.info("토큰이 {} 주제에 성공적으로 구독되었습니다.", topic);
        } catch (FirebaseMessagingException e) {
            log.error("주제 구독 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("주제 구독에 실패했습니다.", e);
        }
    }

    /**
     * 특정 주제에서 FCM 토큰을 구독 해제
     */
    public void unsubscribeFromTopic(String fcmToken, String topic) {
        try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(List.of(fcmToken), topic);
            log.info("토큰이 {} 주제에서 성공적으로 구독 해제되었습니다.", topic);
        } catch (FirebaseMessagingException e) {
            log.error("주제 구독 해제 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("주제 구독 해제에 실패했습니다.", e);
        }
    }
}
