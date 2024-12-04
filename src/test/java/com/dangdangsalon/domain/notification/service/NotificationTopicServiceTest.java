package com.dangdangsalon.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("NotificationTopicService Test")
class NotificationTopicServiceTest {

    private NotificationTopicService notificationTopicService;

    private MockedStatic<FirebaseMessaging> firebaseMessagingMockedStatic;

    private FirebaseMessaging firebaseMessagingMock;

    @BeforeEach
    void setUp() {
        firebaseMessagingMock = mock(FirebaseMessaging.class);
        firebaseMessagingMockedStatic = Mockito.mockStatic(FirebaseMessaging.class);
        firebaseMessagingMockedStatic.when(FirebaseMessaging::getInstance).thenReturn(firebaseMessagingMock);

        notificationTopicService = new NotificationTopicService();
    }

    @AfterEach
    void tearDown() {
        firebaseMessagingMockedStatic.close();
    }

    @Test
    @DisplayName("주제 메시지 전송 성공")
    void sendNotificationToTopic_Success() throws FirebaseMessagingException {
        // Given
        String topic = "contest";
        String title = "새로운 강아지 사진이 올라왔어요";
        String body = "빠르게 확인해 보세요";

        when(firebaseMessagingMock.send(any(Message.class))).thenReturn("test-response");

        // When
        notificationTopicService.sendNotificationToTopic(topic, title, body);

        // Then
        verify(firebaseMessagingMock, times(1)).send(any(Message.class));
    }

    @Test
    @DisplayName("주제 메시지 전송 실패")
    void sendNotificationToTopic_Fail() throws Exception {
        // Given
        String topic = "contest";
        String title = "새로운 강아지 사진이 올라왔어요";
        String body = "빠르게 확인해 보세요";

        doThrow(new RuntimeException("FirebaseMessagingException Mock"))
                .when(firebaseMessagingMock)
                .send(any(Message.class));

        // When & Then
        assertThrows(RuntimeException.class, () -> notificationTopicService.sendNotificationToTopic(topic, title, body));
        verify(firebaseMessagingMock, times(1)).send(any(Message.class));
    }

    @Test
    @DisplayName("주제 구독 성공")
    void subscribeToTopic_Success() throws FirebaseMessagingException {
        // Given
        String fcmToken = "test-token";
        String topic = "contest";

        when(firebaseMessagingMock.subscribeToTopic(anyList(), eq(topic))).thenReturn(null);

        // When
        notificationTopicService.subscribeToTopic(fcmToken, topic);

        // Then
        verify(firebaseMessagingMock, times(1)).subscribeToTopic(anyList(), eq(topic));
    }

    @Test
    @DisplayName("주제 구독 실패")
    void subscribeToTopic_Failure() throws Exception {
        // Given
        String fcmToken = "test-token";
        String topic = "contest";

        doThrow(new RuntimeException("FirebaseMessagingException Mock"))
                .when(firebaseMessagingMock)
                .subscribeToTopic(anyList(), eq(topic));

        // When & Then
        assertThrows(RuntimeException.class, () -> notificationTopicService.subscribeToTopic(fcmToken, topic));
        verify(firebaseMessagingMock, times(1)).subscribeToTopic(anyList(), eq(topic));
    }

    @Test
    @DisplayName("주제 구독 해제 성공")
    void unsubscribeFromTopic_Success() throws FirebaseMessagingException {
        // Given
        String fcmToken = "test-token";
        String topic = "contest";

        when(firebaseMessagingMock.unsubscribeFromTopic(anyList(), eq(topic))).thenReturn(null);

        // When
        notificationTopicService.unsubscribeFromTopic(fcmToken, topic);

        // Then
        verify(firebaseMessagingMock, times(1)).unsubscribeFromTopic(anyList(), eq(topic));
    }

    @Test
    @DisplayName("주제 구독 해제 실패")
    void unsubscribeFromTopic_Failure() throws Exception {
        // Given
        String fcmToken = "test-token";
        String topic = "contest";

        doThrow(new RuntimeException("FirebaseMessagingException Mock"))
                .when(firebaseMessagingMock)
                .unsubscribeFromTopic(anyList(), eq(topic));

        // When & Then
        assertThrows(RuntimeException.class, () -> notificationTopicService.unsubscribeFromTopic(fcmToken, topic));
        verify(firebaseMessagingMock, times(1)).unsubscribeFromTopic(anyList(), eq(topic));
    }

    @Test
    @DisplayName("주제 메시지 전송 실패 - FirebaseMessagingException")
    void sendNotificationToTopic_FirebaseMessagingException() throws FirebaseMessagingException {
        // Given
        String topic = "contest";
        String title = "새로운 강아지 사진이 올라왔어요";
        String body = "빠르게 확인해 보세요";

        FirebaseMessagingException exception = mock(FirebaseMessagingException.class);
        when(exception.getMessage()).thenReturn("Mocked FirebaseMessagingException");

        doThrow(exception).when(firebaseMessagingMock).send(any(Message.class));

        // When & Then
        assertThrows(RuntimeException.class, () -> notificationTopicService.sendNotificationToTopic(topic, title, body));
        verify(firebaseMessagingMock, times(1)).send(any(Message.class));
    }

    @Test
    @DisplayName("주제 구독 실패 - FirebaseMessagingException")
    void subscribeToTopic_FirebaseMessagingException() throws FirebaseMessagingException {
        // Given
        String fcmToken = "test-token";
        String topic = "contest";

        FirebaseMessagingException exception = mock(FirebaseMessagingException.class);
        when(exception.getMessage()).thenReturn("Mocked FirebaseMessagingException");

        doThrow(exception).when(firebaseMessagingMock).subscribeToTopic(anyList(), eq(topic));

        // When & Then
        assertThrows(RuntimeException.class, () -> notificationTopicService.subscribeToTopic(fcmToken, topic));
        verify(firebaseMessagingMock, times(1)).subscribeToTopic(anyList(), eq(topic));
    }

    @Test
    @DisplayName("주제 구독 해제 실패 - FirebaseMessagingException")
    void unsubscribeFromTopic_FirebaseMessagingException() throws FirebaseMessagingException {
        // Given
        String fcmToken = "test-token";
        String topic = "contest";

        FirebaseMessagingException exception = mock(FirebaseMessagingException.class);
        when(exception.getMessage()).thenReturn("Mocked FirebaseMessagingException");

        doThrow(exception).when(firebaseMessagingMock).unsubscribeFromTopic(anyList(), eq(topic));

        // When & Then
        assertThrows(RuntimeException.class, () -> notificationTopicService.unsubscribeFromTopic(fcmToken, topic));
        verify(firebaseMessagingMock, times(1)).unsubscribeFromTopic(anyList(), eq(topic));
    }
}
