package com.dangdangsalon.domain.notification.service;

import com.dangdangsalon.domain.notification.dto.NotificationDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@DisplayName("RedisNotificationService Test")
class RedisNotificationServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ListOperations<String, String> listOperations;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisNotificationService redisNotificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("알림 저장 - 성공")
    void saveNotificationToRedis_Success() throws JsonProcessingException {
        // Given
        Long userId = 1L;
        String title = "견적서를 확인하세요";
        String body = "견적서를 빠르게 확인하세요";
        String type = "estimate";
        Long referenceId = 100L;

        NotificationDto notificationDto = NotificationDto.builder()
                .id(UUID.randomUUID().toString())
                .title(title)
                .body(body)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .type(type)
                .referenceId(referenceId)
                .build();

        String notificationJson = "mocked-json";
        when(objectMapper.writeValueAsString(any(NotificationDto.class))).thenReturn(notificationJson);

        // When
        redisNotificationService.saveNotificationToRedis(userId, title, body, type, referenceId);

        // Then
        verify(listOperations, times(1)).leftPush(eq("notifications:" + userId), eq(notificationJson));
        verify(valueOperations, times(1)).increment(eq("unread_count:" + userId));
    }

    @Test
    @DisplayName("읽지 않은 알림 개수 조회 성공")
    void getUnreadNotificationCount_Success() {
        // Given
        Long userId = 1L;
        when(valueOperations.get("unread_count:" + userId)).thenReturn("5");

        // When
        Long unreadCount = redisNotificationService.getUnreadNotificationCount(userId);

        // Then
        assertThat(unreadCount).isEqualTo(5L);
        verify(valueOperations, times(1)).get("unread_count:" + userId);
    }

    @Test
    @DisplayName("알림 리스트 조회 성공")
    void getNotificationList_Success() throws JsonProcessingException {
        // Given
        Long userId = 1L;
        String notificationJson = "mocked-json";
        NotificationDto mockNotification = NotificationDto.builder()
                .id(UUID.randomUUID().toString())
                .title("견적서를 확인하세요")
                .body("견적서를 빠르게 확인하세요")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .type("estimate")
                .referenceId(100L)
                .build();

        when(listOperations.range("notifications:" + userId, 0, -1))
                .thenReturn(Collections.singletonList(notificationJson));
        when(objectMapper.readValue(notificationJson, NotificationDto.class)).thenReturn(mockNotification);

        // When
        List<NotificationDto> notifications = redisNotificationService.getNotificationList(userId);

        // Then
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getTitle()).isEqualTo("견적서를 확인하세요");
        verify(listOperations, times(1)).range("notifications:" + userId, 0, -1);
    }

    @Test
    @DisplayName("알림 읽음 처리 성공")
    void updateNotificationAsRead_Success() throws JsonProcessingException {
        // Given
        Long userId = 1L;
        String uuid = UUID.randomUUID().toString();
        String notificationJson = "mocked-json";

        NotificationDto mockNotification = NotificationDto.builder()
                .id(uuid)
                .title("견적서를 확인하세요")
                .body("견적서를 빠르게 확인하세요")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .type("estimate")
                .referenceId(100L)
                .build();

        when(listOperations.range("notifications:" + userId, 0, -1))
                .thenReturn(Collections.singletonList(notificationJson));
        when(objectMapper.readValue(notificationJson, NotificationDto.class)).thenReturn(mockNotification);
        when(objectMapper.writeValueAsString(any(NotificationDto.class))).thenReturn(notificationJson);

        // When
        redisNotificationService.updateNotificationAsRead(userId, uuid);

        // Then
        verify(listOperations, times(1)).set(eq("notifications:" + userId), eq(0L), eq(notificationJson));
        verify(valueOperations, times(1)).decrement("unread_count:" + userId);
    }


    @Test
    @DisplayName("모든 알림 읽음 처리 성공")
    void notificationsAsRead_Success() throws JsonProcessingException {
        // Given
        Long userId = 1L;
        String notificationJson = "mocked-json";

        NotificationDto mockNotification = NotificationDto.builder()
                .id(UUID.randomUUID().toString())
                .title("견적서를 확인하세요")
                .body("견적서를 빠르게 확인하세요")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .type("estimate")
                .referenceId(100L)
                .build();

        when(listOperations.range("notifications:" + userId, 0, -1))
                .thenReturn(Collections.singletonList(notificationJson));
        when(objectMapper.readValue(notificationJson, NotificationDto.class)).thenReturn(mockNotification);
        when(objectMapper.writeValueAsString(any(NotificationDto.class))).thenReturn(notificationJson);

        // When
        redisNotificationService.notificationsAsRead(userId);

        // Then
        verify(listOperations, times(1)).set(eq("notifications:" + userId), eq(0L), eq(notificationJson));
        verify(valueOperations, times(1)).set("unread_count:" + userId, "0");
    }

    @Test
    @DisplayName("알림 저장 실패 - JsonProcessingException 발생")
    void saveNotificationToRedis_Fail() throws JsonProcessingException {
        // Given
        Long userId = 1L;
        String title = "견적서를 확인하세요";
        String body = "견적서를 빠르게 확인하세요";
        String type = "estimate";
        Long referenceId = 100L;

        JsonProcessingException exception = mock(JsonProcessingException.class);

        when(objectMapper.writeValueAsString(any(NotificationDto.class))).thenThrow(exception);

        // When
        redisNotificationService.saveNotificationToRedis(userId, title, body, type, referenceId);

        // Then
        verify(listOperations, never()).leftPush(anyString(), anyString());
        verify(valueOperations, never()).increment(anyString());
        verifyNoMoreInteractions(listOperations, valueOperations);
    }

    @Test
    @DisplayName("알림 읽음 처리 실패 - JsonProcessingException 발생")
    void updateNotificationAsRead_Fail() throws JsonProcessingException {
        // Given
        Long userId = 1L;
        String uuid = UUID.randomUUID().toString();
        String notificationJson = "mocked-json";

        NotificationDto mockNotification = NotificationDto.builder()
                .id(uuid)
                .title("견적서를 확인하세요")
                .body("견적서를 빠르게 확인하세요")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .type("estimate")
                .referenceId(100L)
                .build();

        when(listOperations.range("notifications:" + userId, 0, -1))
                .thenReturn(Collections.singletonList(notificationJson));
        when(objectMapper.readValue(notificationJson, NotificationDto.class))
                .thenReturn(mockNotification);
        when(objectMapper.writeValueAsString(any(NotificationDto.class)))
                .thenThrow(new JsonProcessingException("Mocked exception") {});

        // When & Then
        assertThrows(RuntimeException.class, () -> redisNotificationService.updateNotificationAsRead(userId, uuid));

        // Verify that no Redis operations were performed after the exception
        verify(listOperations, never()).set(anyString(), anyLong(), anyString());
        verify(valueOperations, never()).decrement(anyString());
    }


    @Test
    @DisplayName("모든 알림 읽음 처리 실패 - JsonProcessingException 발생")
    void notificationsAsRead_Fail() throws JsonProcessingException {
        // Given
        Long userId = 1L;
        String notificationJson = "mocked-json";

        NotificationDto mockNotification = NotificationDto.builder()
                .id(UUID.randomUUID().toString())
                .title("견적서를 확인하세요")
                .body("견적서를 빠르게 확인하세요")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .type("estimate")
                .referenceId(100L)
                .build();

        when(listOperations.range("notifications:" + userId, 0, -1))
                .thenReturn(Collections.singletonList(notificationJson));
        when(objectMapper.readValue(notificationJson, NotificationDto.class))
                .thenReturn(mockNotification);
        when(objectMapper.writeValueAsString(any(NotificationDto.class)))
                .thenThrow(new JsonProcessingException("Mocked exception") {});

        // When
        assertThrows(RuntimeException.class, () -> redisNotificationService.notificationsAsRead(userId));

        // Then
        verify(listOperations, never()).set(anyString(), anyLong(), anyString());
        verify(valueOperations, never()).set("unread_count:" + userId, "0");
    }

}
