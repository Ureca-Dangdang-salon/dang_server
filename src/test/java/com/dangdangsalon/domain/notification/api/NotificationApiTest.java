package com.dangdangsalon.domain.notification.api;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.notification.dto.FcmTokenRequestDto;
import com.dangdangsalon.domain.notification.dto.FcmTokenTopicRequestDto;
import com.dangdangsalon.domain.notification.dto.NotificationDto;
import com.dangdangsalon.domain.notification.service.NotificationService;
import com.dangdangsalon.domain.notification.service.NotificationTopicService;
import com.dangdangsalon.domain.notification.service.RedisNotificationService;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class NotificationApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NotificationTopicService notificationTopicService;

    @MockBean
    private RedisNotificationService redisNotificationService;

    @BeforeEach
    void setup() {
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("FCM 토큰 등록 테스트")
    void registerFcmToken() {
        CustomOAuth2User mockLoginUser = Mockito.mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        FcmTokenRequestDto requestDto = new FcmTokenRequestDto("test-fcm-token");

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .body(requestDto)
                .when()
                .post("/api/notification/fcm-token")
                .then()
                .statusCode(200)
                .body("response", equalTo("FCM 토큰이 성공적으로 등록되었습니다."));

        verify(notificationService).saveOrUpdateFcmToken(any(Long.class), anyString());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("읽지 않은 알림 개수 조회 테스트")
    void getUnreadNotificationCount() {
        CustomOAuth2User mockLoginUser = Mockito.mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        given(redisNotificationService.getUnreadNotificationCount(any(Long.class))).willReturn(5L);

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/notification/unread-count")
                .then()
                .statusCode(200)
                .body("response", equalTo(5));

        verify(redisNotificationService).getUnreadNotificationCount(any(Long.class));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("알림 목록 조회 테스트")
    void getNotificationList() {
        CustomOAuth2User mockLoginUser = Mockito.mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        // Mock 데이터 생성
        NotificationDto mockNotification = NotificationDto.builder()
                .id("uuid1")
                .title("새로운 알림")
                .body("알림 내용")
                .isRead(false)
                .createdAt(LocalDateTime.of(2024, 12, 1, 10, 0, 0))
                .type("INFO")
                .referenceId(123L)
                .build();

        List<NotificationDto> mockResponse = List.of(mockNotification);

        given(redisNotificationService.getNotificationList(any(Long.class))).willReturn(mockResponse);

        // 테스트 실행 및 검증
        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/notification/list")
                .then()
                .statusCode(200)
                .body("response", hasSize(1))
                .body("response[0].id", equalTo("uuid1"))
                .body("response[0].title", equalTo("새로운 알림"))
                .body("response[0].body", equalTo("알림 내용"))
                .body("response[0].read", equalTo(false))
                .body("response[0].createdAt", equalTo("2024-12-01T10:00:00"))
                .body("response[0].type", equalTo("INFO"))
                .body("response[0].referenceId", equalTo(123));

        // 서비스 호출 검증
        verify(redisNotificationService).getNotificationList(any(Long.class));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("알림 읽음 처리 테스트")
    void updateNotificationAsRead() {
        CustomOAuth2User mockLoginUser = Mockito.mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .queryParam("uuid", "uuid1")
                .when()
                .post("/api/notification/read")
                .then()
                .statusCode(200)
                .body("response", equalTo("알림이 성공적으로 읽음 처리되었습니다."));

        verify(redisNotificationService).updateNotificationAsRead(any(Long.class), anyString());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("모든 알림 읽음 처리 테스트")
    void markAllNotificationsAsRead() {
        CustomOAuth2User mockLoginUser = Mockito.mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .when()
                .post("/api/notification/read-all")
                .then()
                .statusCode(200)
                .body("response", equalTo("모든 알림이 성공적으로 읽음 처리되었습니다."));

        verify(redisNotificationService).notificationsAsRead(any(Long.class));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("구독 테스트")
    void subscribe() {
        CustomOAuth2User mockLoginUser = Mockito.mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        FcmTokenTopicRequestDto requestDto = new FcmTokenTopicRequestDto("test-fcm-token", "test-topic");

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .body(requestDto)
                .when()
                .post("/api/notification/subscribe")
                .then()
                .statusCode(200)
                .body("response", equalTo("성공적으로 구독되었습니다."));

        verify(notificationService).saveOrUpdateFcmToken(any(Long.class), anyString());
        verify(notificationTopicService).subscribeToTopic(anyString(), anyString());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("구독 해제 테스트")
    void unsubscribe() {
        FcmTokenTopicRequestDto requestDto = new FcmTokenTopicRequestDto("test-fcm-token", "test-topic");

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .body(requestDto)
                .when()
                .post("/api/notification/unsubscribe")
                .then()
                .statusCode(200)
                .body("response", equalTo("구독이 해제되었습니다."));

        verify(notificationTopicService).unsubscribeFromTopic(anyString(), anyString());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    @DisplayName("알림 설정 업데이트 테스트")
    void updateUserNotification() {
        CustomOAuth2User mockLoginUser = Mockito.mock(CustomOAuth2User.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockLoginUser, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        RestAssuredMockMvc
                .given()
                .contentType(ContentType.JSON)
                .when()
                .post("/api/notification/update/true")
                .then()
                .statusCode(200)
                .body("response", equalTo("알림 설정이 업데이트 되었습니다."));

        verify(notificationService).updateUserNotification(any(Long.class), any(Boolean.class));
    }
}