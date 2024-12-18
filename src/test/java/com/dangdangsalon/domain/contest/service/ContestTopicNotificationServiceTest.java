package com.dangdangsalon.domain.contest.service;

import com.dangdangsalon.domain.notification.service.NotificationTopicService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

class ContestTopicNotificationServiceTest {

    @Mock
    private NotificationTopicService notificationTopicService;

    @InjectMocks
    private ContestTopicNotificationService contestTopicNotificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("구독 푸시 알림 보내기 - 성공")
    void sendContestJoinNotification_success() {
        // Arrange
        String expectedTopic = "contest";
        String expectedTitle = "새로운 게시글이 올라왔어요!!";
        String expectedBody = "귀여운 강아지 사진이 올라왔어요!! 빠르게 확인해보세요";

        // Act
        contestTopicNotificationService.sendContestJoinNotification();

        // Assert
        verify(notificationTopicService, times(1))
                .sendNotificationToTopic(expectedTopic, expectedTitle, expectedBody);
    }

    @Test
    @DisplayName("구독 푸시 알림 보내기 - 실패")
    void sendContestJoinNotification_fail() {
        // Arrange
        String expectedTopic = "contest";
        String expectedTitle = "새로운 게시글이 올라왔어요!!";
        String expectedBody = "귀여운 강아지 사진이 올라왔어요!! 빠르게 확인해보세요";

        doThrow(new RuntimeException("Test exception"))
                .when(notificationTopicService)
                .sendNotificationToTopic(expectedTopic, expectedTitle, expectedBody);

        // Act
        contestTopicNotificationService.sendContestJoinNotification();

        // Assert
        verify(notificationTopicService, times(1))
                .sendNotificationToTopic(expectedTopic, expectedTitle, expectedBody);
    }
}
