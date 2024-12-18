package com.dangdangsalon.domain.notification.controller;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.notification.dto.FcmTokenRequestDto;
import com.dangdangsalon.domain.notification.dto.FcmTokenTopicRequestDto;
import com.dangdangsalon.domain.notification.dto.NotificationDto;
import com.dangdangsalon.domain.notification.service.NotificationService;
import com.dangdangsalon.domain.notification.service.NotificationTopicService;
import com.dangdangsalon.domain.notification.service.RedisNotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@ActiveProfiles("test")
@MockBean(JpaMetamodelMappingContext.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NotificationTopicService notificationTopicService;

    @MockBean
    private RedisNotificationService redisNotificationService;

    private CustomOAuth2User createAndSetupMockUser() {
        CustomOAuth2User mockLoginUser = Mockito.mock(CustomOAuth2User.class);
        Mockito.when(mockLoginUser.getUserId()).thenReturn(1L);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        return mockLoginUser;
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("FCM 토큰 등록 테스트")
    void registerFcmToken() throws Exception {
        createAndSetupMockUser();

        FcmTokenRequestDto requestDto = FcmTokenRequestDto.builder()
                .fcmToken("test-token")
                .build();

        mockMvc.perform(post("/api/notification/fcm-token")
                        .contentType("application/json")
                        .content("{\"fcmToken\":\"test-token\"}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("FCM 토큰이 성공적으로 등록되었습니다."));

        verify(notificationService, times(1)).saveOrUpdateFcmToken(eq(1L), eq(requestDto.getFcmToken()));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("읽지 않은 알림 개수 조회 테스트")
    void getUnreadNotificationCount() throws Exception {
        createAndSetupMockUser();

        when(redisNotificationService.getUnreadNotificationCount(eq(1L))).thenReturn(5L);

        mockMvc.perform(get("/api/notification/unread-count").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(5L));

        verify(redisNotificationService, times(1)).getUnreadNotificationCount(eq(1L));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("알림 리스트 조회 테스트")
    void getNotificationList() throws Exception {
        createAndSetupMockUser();

        NotificationDto notification1 = NotificationDto.builder()
                .id("dsdsadsa")
                .title("견적서를 확인하세요")
                .body("Body1")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .type("type1")
                .referenceId(1L)
                .build();

        NotificationDto notification2 = NotificationDto.builder()
                .id("sdadasdsa")
                .title("리뷰를 작성해주세요")
                .body("Body2")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .type("type2")
                .referenceId(2L)
                .build();

        when(redisNotificationService.getNotificationList(eq(1L))).thenReturn(List.of(notification1, notification2));

        mockMvc.perform(get("/api/notification/list").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response[0].title").value("견적서를 확인하세요"))
                .andExpect(jsonPath("$.response[1].title").value("리뷰를 작성해주세요"));

        verify(redisNotificationService, times(1)).getNotificationList(eq(1L));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("특정 알림 읽음 처리 테스트")
    void updateNotificationAsRead() throws Exception {
        createAndSetupMockUser();

        mockMvc.perform(post("/api/notification/read")
                        .param("uuid", "test-uuid")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("알림이 성공적으로 읽음 처리되었습니다."));

        verify(redisNotificationService, times(1)).updateNotificationAsRead(eq(1L), eq("test-uuid"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("모든 알림 읽음 처리 테스트")
    void markAllNotificationsAsRead() throws Exception {
        createAndSetupMockUser();

        mockMvc.perform(post("/api/notification/read-all").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("모든 알림이 성공적으로 읽음 처리되었습니다."));

        verify(redisNotificationService, times(1)).notificationsAsRead(eq(1L));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("주제 구독 테스트")
    void subscribeToTopic() throws Exception {
        createAndSetupMockUser();

        FcmTokenTopicRequestDto requestDto = FcmTokenTopicRequestDto.builder()
                .fcmToken("test-token")
                .topic("test-topic")
                .build();

        mockMvc.perform(post("/api/notification/subscribe")
                        .contentType("application/json")
                        .content("{\"fcmToken\":\"test-token\",\"topic\":\"test-topic\"}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("성공적으로 구독되었습니다."));

        verify(notificationService, times(1)).saveOrUpdateFcmToken(eq(1L), eq(requestDto.getFcmToken()));
        verify(notificationTopicService, times(1))
                .subscribeToTopicInApp(eq(requestDto.getFcmToken()), eq(requestDto.getTopic()), eq(1L));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("주제 구독 해제 테스트")
    void unsubscribeFromTopic() throws Exception {
        createAndSetupMockUser();

        FcmTokenTopicRequestDto requestDto = FcmTokenTopicRequestDto.builder()
                .fcmToken("test-token")
                .topic("test-topic")
                .build();

        mockMvc.perform(post("/api/notification/unsubscribe")
                        .contentType("application/json")
                        .content("{\"fcmToken\":\"test-token\",\"topic\":\"test-topic\"}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("구독이 해제되었습니다."));

        verify(notificationTopicService, times(1))
                .unsubscribeFromTopicInApp(eq(requestDto.getFcmToken()), eq(requestDto.getTopic()), eq(1L));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("사용자 알림 설정 업데이트 테스트")
    void updateUserNotification() throws Exception {
        createAndSetupMockUser();

        mockMvc.perform(post("/api/notification/update/true").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value("알림 설정이 업데이트 되었습니다."));

        verify(notificationService, times(1)).updateUserNotification(eq(1L), eq(true));
    }
}