package com.dangdangsalon.domain.notification.controller;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.notification.dto.FcmTokenRequestDto;
import com.dangdangsalon.domain.notification.service.NotificationScheduler;
import com.dangdangsalon.domain.notification.service.NotificationService;
import com.dangdangsalon.domain.notification.service.NotificationTopicService;
import com.dangdangsalon.util.ApiUtil;
import com.dangdangsalon.util.ApiUtil.ApiSuccess;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationTopicService notificationTopicService;


    /**
     * 로그인 시 FCM 토큰을 Redis에 저장
     */
    @PostMapping("/fcm-token")
    public ApiSuccess<?> registerFcmToken(@RequestBody FcmTokenRequestDto requestDto,
                                          @AuthenticationPrincipal CustomOAuth2User user) {

        Long userId = user.getUserId();
        notificationService.saveOrUpdateFcmToken(userId, requestDto.getFcmToken());

        return ApiUtil.success("FCM 토큰이 성공적으로 등록되었습니다.");
    }

    /**
     * 읽지 않은 알림 개수 조회
     */
    @GetMapping("/unread-count")
    public ApiSuccess<?> getUnreadNotificationCount(@AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();
        Long unreadCount = notificationService.getUnreadNotificationCount(userId);
        return ApiUtil.success(unreadCount);
    }

    /**
     * 알림 리스트 가져오기
     */
    @GetMapping("/list")
    public ApiSuccess<?> getNotificationList(@AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();
        List<Map<String, Object>> notifications = notificationService.getNotificationList(userId);
        return ApiUtil.success(notifications);
    }

    /**
     * 알림 읽음 처리
     */
    @PostMapping("/read")
    public ApiSuccess<?> updateNotificationAsRead(@RequestParam String uuid,
                                                @AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();
        notificationService.updateNotificationAsRead(userId, uuid);
        return ApiUtil.success("알림이 성공적으로 읽음 처리되었습니다.");
    }

    /**
     * 모든 알림 읽음 처리
     */
    @PostMapping("/read-all")
    public ApiSuccess<?> markAllNotificationsAsRead(@AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();
        notificationService.notificationsAsRead(userId);
        return ApiUtil.success("모든 알림이 성공적으로 읽음 처리되었습니다.");
    }

    /**
     * 페스티벌 주제 구독
     */
    @PostMapping("/subscribe/festival")
    public ApiSuccess<?> subscribeToFestival(@AuthenticationPrincipal CustomOAuth2User user, @RequestBody FcmTokenRequestDto requestDto) {
        Long userId = user.getUserId();

        notificationService.saveOrUpdateFcmToken(userId, requestDto.getFcmToken());
        notificationTopicService.subscribeToTopic(requestDto.getFcmToken(), "festival");

        return ApiUtil.success("페스티벌 주제에 성공적으로 구독되었습니다.");
    }

    /**
     * 페스티벌 주제 구독 해제
     */
    @PostMapping("/unsubscribe/festival")
    public ApiSuccess<?> unsubscribeFromFestival(@RequestBody FcmTokenRequestDto requestDto) {
        // 주제 구독 해제
        notificationTopicService.unsubscribeFromTopic(requestDto.getFcmToken(), "festival");

        return ApiUtil.success("페스티벌 주제 구독이 해제되었습니다.");
    }

    /**
     * 주제로 알림 보내기
     */
    @PostMapping("/send")
    public ApiSuccess<?> sendNotificationToTopic(@RequestParam String topic,
                                                 @RequestParam String title,
                                                 @RequestParam String body) {
        notificationTopicService.sendNotificationToTopic(topic, title, body);
        return ApiUtil.success("알림이 성공적으로 전송되었습니다.");
    }
}
