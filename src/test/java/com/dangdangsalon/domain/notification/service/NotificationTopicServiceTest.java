package com.dangdangsalon.domain.notification.service;

import com.dangdangsalon.domain.notification.entity.Topic;
import com.dangdangsalon.domain.notification.repository.TopicRepository;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("NotificationTopicService Test")
class NotificationTopicServiceTest {

    private NotificationTopicService notificationTopicService;

    private MockedStatic<FirebaseMessaging> firebaseMessagingMockedStatic;

    @Mock
    private FirebaseMessaging firebaseMessagingMock;

    @Mock
    private TopicRepository topicRepositoryMock;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private UserRepository userRepositoryMock;

    @BeforeEach
    void setUp() {
        // Initialize Mockito annotations
        MockitoAnnotations.openMocks(this);

        // Mock the static FirebaseMessaging method
        firebaseMessagingMockedStatic = Mockito.mockStatic(FirebaseMessaging.class);
        firebaseMessagingMockedStatic.when(FirebaseMessaging::getInstance).thenReturn(firebaseMessagingMock);

        // Create the service with mocked dependencies
        notificationTopicService = new NotificationTopicService(
                topicRepositoryMock,
                userRepositoryMock
        );
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

    @Test
    @DisplayName("앱 내 주제 구독 성공")
    void subscribeToTopicInApp_Success() throws FirebaseMessagingException {
        // Given
        String fcmToken = "test-token";
        String topicName = "contest";
        Long userId = 1L;

        User mockUser = mock(User.class);
        Topic mockTopic = mock(Topic.class);

        when(userRepositoryMock.findById(userId)).thenReturn(Optional.of(mockUser));
        when(topicRepositoryMock.findByTopicNameAndUser(topicName, mockUser)).thenReturn(Optional.empty());
        when(firebaseMessagingMock.subscribeToTopic(List.of(fcmToken), topicName)).thenReturn(null);

        // When
        notificationTopicService.subscribeToTopicInApp(fcmToken, topicName, userId);

        // Then
        verify(userRepositoryMock, times(1)).findById(userId);
        verify(firebaseMessagingMock, times(1)).subscribeToTopic(List.of(fcmToken), topicName);
        verify(topicRepositoryMock, times(1)).save(any(Topic.class));
    }

    @Test
    @DisplayName("앱 내 주제 구독 해제 성공")
    void unsubscribeFromTopicInApp_Success() throws FirebaseMessagingException {
        // Given
        String fcmToken = "test-token";
        String topicName = "contest";
        Long userId = 1L;

        User mockUser = mock(User.class);
        Topic mockTopic = mock(Topic.class);

        when(userRepositoryMock.findById(userId)).thenReturn(Optional.of(mockUser));
        when(topicRepositoryMock.findByTopicNameAndUser(topicName, mockUser)).thenReturn(Optional.of(mockTopic));
        when(firebaseMessagingMock.unsubscribeFromTopic(List.of(fcmToken), topicName)).thenReturn(null);

        // When
        notificationTopicService.unsubscribeFromTopicInApp(fcmToken, topicName, userId);

        // Then
        verify(userRepositoryMock, times(1)).findById(userId);
        verify(firebaseMessagingMock, times(1)).unsubscribeFromTopic(List.of(fcmToken), topicName);
        verify(mockTopic, times(1)).updateSubscribe(false);
    }

    @Test
    @DisplayName("앱 내 주제 구독 해제 실패 - 토픽 없음")
    void unsubscribeFromTopicInApp_TopicNotFound() {
        // Given
        String fcmToken = "test-token";
        String topicName = "contest";
        Long userId = 1L;

        User mockUser = mock(User.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(topicRepository.findByTopicNameAndUser(topicName, mockUser)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> notificationTopicService.unsubscribeFromTopicInApp(fcmToken, topicName, userId));
    }

    @Test
    @DisplayName("사용자 찾기 실패 - 구독 상태 확인 중")
    void isSubscribed_UserNotFound() {
        // Given
        String topicName = "contest";
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> notificationTopicService.isSubscribed(topicName, userId));
    }
}
