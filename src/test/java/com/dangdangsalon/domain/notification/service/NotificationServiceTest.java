package com.dangdangsalon.domain.notification.service;

import com.dangdangsalon.domain.notification.dto.ReviewNotificationDto;
import com.dangdangsalon.domain.notification.entity.FcmToken;
import com.dangdangsalon.domain.notification.repository.FcmTokenRepository;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@DisplayName("NotificationService Test")
class NotificationServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private FcmTokenRepository fcmTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private MockedStatic<FirebaseMessaging> firebaseMessagingMockedStatic;
    private FirebaseMessaging firebaseMessagingMock;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        firebaseMessagingMock = mock(FirebaseMessaging.class);
        firebaseMessagingMockedStatic = Mockito.mockStatic(FirebaseMessaging.class);
        firebaseMessagingMockedStatic.when(FirebaseMessaging::getInstance).thenReturn(firebaseMessagingMock);
    }

    @AfterEach
    void tearDown() {
        firebaseMessagingMockedStatic.close();
    }

    @Test
    @DisplayName("FCM 알림 전송 성공")
    void sendNotificationWithData_Success() throws FirebaseMessagingException {
        // Given
        String token = "valid-token";
        String title = "한유성 미용사님이 견적서를 보냈어요";
        String body = "빠르게 확인해보세요";
        String type = "TEST";
        Long referenceId = 123L;

        User mockUser = mock(User.class);
        when(mockUser.getNotificationEnabled()).thenReturn(true);

        FcmToken mockFcmToken = FcmToken.builder().fcmToken(token).user(mockUser).build();
        when(fcmTokenRepository.findByFcmToken(token)).thenReturn(Optional.of(mockFcmToken));

        // When
        notificationService.sendNotificationWithData(token, title, body, type, referenceId);

        // Then
        verify(fcmTokenRepository, times(1)).findByFcmToken(token);
        verify(firebaseMessagingMock, times(1)).send(any(Message.class));
    }

    @Test
    @DisplayName("FCM 토큰 없음 예외")
    void sendNotificationWithData_TokenNotFound() {
        // Given
        String token = "non-existent-token";
        String title = "한유성 미용사님이 견적서를 보냈어요";
        String body = "빠르게 확인해보세요";
        String type = "TEST";
        Long referenceId = 123L;

        when(fcmTokenRepository.findByFcmToken(token)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                notificationService.sendNotificationWithData(token, title, body, type, referenceId)
        );
    }

    @Test
    @DisplayName("알림 비활성화 사용자")
    void sendNotificationWithData_NotificationDisabled() throws FirebaseMessagingException {
        // Given
        String token = "disabled-token";
        String title = "한유성 미용사님이 견적서를 보냈어요";
        String body = "빠르게 확인해보세요";
        String type = "TEST";
        Long referenceId = 123L;

        User mockUser = mock(User.class);
        when(mockUser.getNotificationEnabled()).thenReturn(false);

        FcmToken mockFcmToken = FcmToken.builder().fcmToken(token).user(mockUser).build();
        when(fcmTokenRepository.findByFcmToken(token)).thenReturn(Optional.of(mockFcmToken));

        // When
        notificationService.sendNotificationWithData(token, title, body, type, referenceId);

        // Then
        verify(fcmTokenRepository, times(1)).findByFcmToken(token);
        verify(firebaseMessagingMock, never()).send(any(Message.class));
    }

    @Test
    @DisplayName("FCM 알림 전송 실패 - 유효하지 않은 토큰")
    void sendNotificationWithData_InvalidToken() throws FirebaseMessagingException {
        // Given
        String token = "invalid-token";
        String title = "한유성 미용사님이 견적서를 보냈어요";
        String body = "빠르게 확인해보세요";
        String type = "TEST";
        Long referenceId = 123L;

        User mockUser = mock(User.class);
        when(mockUser.getNotificationEnabled()).thenReturn(true);

        FcmToken mockFcmToken = FcmToken.builder().fcmToken(token).user(mockUser).build();
        when(fcmTokenRepository.findByFcmToken(token)).thenReturn(Optional.of(mockFcmToken));

        FirebaseMessagingException invalidArgumentException = mock(FirebaseMessagingException.class);
        when(invalidArgumentException.getMessagingErrorCode()).thenReturn(MessagingErrorCode.INVALID_ARGUMENT);

        when(firebaseMessagingMock.send(any(Message.class))).thenThrow(invalidArgumentException);

        // When
        notificationService.sendNotificationWithData(token, title, body, type, referenceId);

        // Then
        verify(fcmTokenRepository, times(1)).deleteByFcmToken(token);
    }

    @Test
    @DisplayName("FCM 토큰 저장/업데이트 성공")
    void saveOrUpdateFcmToken_Success() {
        // Given
        Long userId = 1L;
        String token = "new-token";

        User mockUser = mock(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(fcmTokenRepository.findByFcmToken(token)).thenReturn(Optional.empty());

        // When
        notificationService.saveOrUpdateFcmToken(userId, token);

        // Then
        verify(fcmTokenRepository, times(1)).save(any(FcmToken.class));
    }

    @Test
    @DisplayName("읽지 않은 알림 예약 성공")
    void scheduleReviewNotification_Success() {
        // Given
        Long userId = 1L;
        Long estimateId = 2L;

        ReviewNotificationDto reminderData = ReviewNotificationDto.builder()
                .userId(userId)
                .estimateId(estimateId)
                .scheduledTime(LocalDateTime.now().plusMinutes(30).toString())
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // When
        notificationService.scheduleReviewNotification(userId, estimateId);

        // Then
        verify(valueOperations, times(1)).set(eq("review_notification:" + estimateId), anyString());
    }
}