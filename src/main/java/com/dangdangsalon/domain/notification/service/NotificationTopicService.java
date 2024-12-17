package com.dangdangsalon.domain.notification.service;

import com.dangdangsalon.domain.notification.entity.Topic;
import com.dangdangsalon.domain.notification.repository.TopicRepository;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationTopicService {

    private final TopicRepository topicRepository;
    private final UserRepository userRepository;

    @Transactional
    public void subscribeToTopicInApp(String fcmToken, String topicName, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다: " + userId));

        subscribeToTopic(fcmToken, topicName);

        Topic topic = topicRepository.findByTopicNameAndUser(topicName, user)
                .orElse(Topic.builder()
                        .topicName(topicName)
                        .subscribe(true)
                        .user(user)
                        .build());

        topic.updateSubscribe(true);
        topicRepository.save(topic);
    }

    @Transactional
    public void unsubscribeFromTopicInApp(String fcmToken, String topicName, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다: " + userId));

        unsubscribeFromTopic(fcmToken, topicName);

        Topic topic = topicRepository.findByTopicNameAndUser(topicName, user)
                .orElseThrow(() -> new IllegalArgumentException("구독 중인 주제를 찾을 수 없습니다."));

        topic.updateSubscribe(false);
    }

    public boolean isSubscribed(String topicName, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다: " + userId));

        return topicRepository.findByTopicNameAndUser(topicName, user)
                .map(Topic::getSubscribe)
                .orElse(false);
    }

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
