package com.dangdangsalon.domain.notification.service;

import com.dangdangsalon.domain.notification.dto.ReviewNotificationDto;
import com.dangdangsalon.domain.notification.entity.FcmToken;
import com.dangdangsalon.domain.notification.repository.FcmTokenRepository;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import com.google.firebase.messaging.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.List;
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

    @Test
    @DisplayName("비활성 토큰 삭제 성공")
    void removeInactiveTokens_Success() {
        // Given
        FcmToken oldToken = FcmToken.builder().fcmToken("old-token").lastUserAt(LocalDateTime.now().minusDays(61)).build();
        FcmToken recentToken = FcmToken.builder().fcmToken("recent-token").lastUserAt(LocalDateTime.now().minusDays(30)).build();

        when(fcmTokenRepository.findAll()).thenReturn(List.of(oldToken, recentToken));

        // When
        notificationService.removeInactiveTokens();

        // Then
        verify(fcmTokenRepository, times(1)).deleteAll(List.of(oldToken));
    }

    @Test
    @DisplayName("FCM 토큰 삭제 성공")
    void deleteFcmToken_Success() {
        // Given
        String token = "delete-token";

        // When
        notificationService.deleteFcmToken(token);

        // Then
        verify(fcmTokenRepository, times(1)).deleteByFcmToken(token);
    }

    @Test
    @DisplayName("FCM 토큰 조회 성공")
    void getFcmToken_Success() {
        // Given
        Long userId = 1L;
        String token = "fcm-token";

        FcmToken mockFcmToken = FcmToken.builder()
                .fcmToken(token)
                .user(mock(User.class))
                .build();

        when(fcmTokenRepository.findByUserId(userId)).thenReturn(Optional.of(mockFcmToken));

        // When
        Optional<String> result = notificationService.getFcmToken(userId);

        // Then
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(token, result.get());
        verify(fcmTokenRepository, times(1)).findByUserId(userId);
    }

    @Test
    @DisplayName("사용자 알림 설정 업데이트 - 활성화")
    void updateUserNotification_Enable() {
        // Given
        Long userId = 1L;
        User mockUser = mock(User.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // When
        notificationService.updateUserNotification(userId, true);

        // Then
        verify(mockUser, times(1)).updateNotificationEnabled(true);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("사용자 알림 설정 업데이트 - 비활성화")
    void updateUserNotification_Disable() {
        // Given
        Long userId = 1L;
        User mockUser = mock(User.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // When
        notificationService.updateUserNotification(userId, false);

        // Then
        verify(mockUser, times(1)).updateNotificationEnabled(false);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("기존 토큰이 다른 사용자와 연결된 경우 - 기존 토큰 삭제 후 새로 저장")
    void saveOrUpdateFcmToken_OtherUserToken() {
        // Given
        Long userId = 1L;
        String token = "shared-token";

        User currentUser = mock(User.class);
        User otherUser = mock(User.class);

        when(currentUser.getId()).thenReturn(userId);
        when(otherUser.getId()).thenReturn(2L);

        when(userRepository.findById(userId)).thenReturn(Optional.of(currentUser));

        FcmToken existingToken = FcmToken.builder().fcmToken(token).user(otherUser).build();
        when(fcmTokenRepository.findByFcmToken(token)).thenReturn(Optional.of(existingToken));

        // When
        notificationService.saveOrUpdateFcmToken(userId, token);

        // Then
        verify(fcmTokenRepository, times(1)).delete(existingToken);
        verify(fcmTokenRepository, times(1)).save(any(FcmToken.class));
    }

    @Test
    @DisplayName("기존 토큰이 동일 사용자와 연결된 경우 - 갱신")
    void saveOrUpdateFcmToken_SameUserToken() {
        // Given
        Long userId = 1L;
        String token = "user-token";

        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        FcmToken existingToken = spy(FcmToken.builder().fcmToken(token).user(mockUser).build());
        when(fcmTokenRepository.findByFcmToken(token)).thenReturn(Optional.of(existingToken));

        // When
        notificationService.saveOrUpdateFcmToken(userId, token);

        // Then
        verify(existingToken, times(1)).updateTokenLastUserAt();
        verify(fcmTokenRepository, never()).save(any(FcmToken.class));
    }

    @Test
    @DisplayName("새로운 토큰 저장")
    void saveOrUpdateFcmToken_NewToken() {
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
    @DisplayName("FCM 알림 전송 실패 - UNREGISTERED 토큰")
    void sendNotificationWithData_UnregisteredToken() throws FirebaseMessagingException {
        // Given
        String token = "unregistered-token";
        String title = "알림 제목";
        String body = "알림 내용";
        String type = "TEST";
        Long referenceId = 123L;

        User mockUser = mock(User.class);
        when(mockUser.getNotificationEnabled()).thenReturn(true);

        FcmToken mockFcmToken = FcmToken.builder().fcmToken(token).user(mockUser).build();
        when(fcmTokenRepository.findByFcmToken(token)).thenReturn(Optional.of(mockFcmToken));

        FirebaseMessagingException unregisteredException = mock(FirebaseMessagingException.class);
        when(unregisteredException.getMessagingErrorCode()).thenReturn(MessagingErrorCode.UNREGISTERED);
        when(firebaseMessagingMock.send(any(Message.class))).thenThrow(unregisteredException);

        // When
        notificationService.sendNotificationWithData(token, title, body, type, referenceId);

        // Then
        verify(fcmTokenRepository, times(1)).deleteByFcmToken(token);
        verify(firebaseMessagingMock, times(1)).send(any(Message.class));
    }
}
