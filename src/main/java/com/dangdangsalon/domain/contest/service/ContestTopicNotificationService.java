package com.dangdangsalon.domain.contest.service;

import com.dangdangsalon.domain.notification.service.NotificationTopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContestTopicNotificationService {

    private final NotificationTopicService notificationTopicService;

    /**
     * 새로운 게시글 올라오면 구독자들에게 알림 전송
     */
    public void sendContestJoinNotification() {
        String topic = "contest";
        String title = "새로운 게시글이 올라왔어요!!";
        String body = "귀여운 강아지 사진이 올라왔어요!! 빠르게 확인해보세요";

        try {
            notificationTopicService.sendNotificationToTopic(topic, title, body);
            log.info("'{}' 주제에 알림 전송 완료.", topic);
        } catch (RuntimeException e) {
            log.error("'{}' 주제에 알림 전송 실패: {}", topic, e.getMessage(), e);
        }
    }
}