package com.dangdangsalon.domain.notification.controller;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.notification.dto.FcmTokenRequestDto;
import com.dangdangsalon.domain.notification.dto.FcmTokenTopicRequestDto;
import com.dangdangsalon.domain.notification.dto.NotificationDto;
import com.dangdangsalon.domain.notification.service.NotificationService;
import com.dangdangsalon.domain.notification.service.NotificationTopicService;
import com.dangdangsalon.util.ApiUtil;
import com.dangdangsalon.util.ApiUtil.ApiSuccess;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationTopicService notificationTopicService;

    @PostMapping("/fcm-token")
    public ApiSuccess<?> registerFcmToken(@RequestBody FcmTokenRequestDto requestDto, @AuthenticationPrincipal CustomOAuth2User user) {

        Long userId = user.getUserId();
        notificationService.saveOrUpdateFcmToken(userId, requestDto.getFcmToken());

        return ApiUtil.success("FCM 토큰이 성공적으로 등록되었습니다.");
    }

    @GetMapping("/unread-count")
    public ApiSuccess<?> getUnreadNotificationCount(@AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();
        Long unreadCount = notificationService.getUnreadNotificationCount(userId);
        return ApiUtil.success(unreadCount);
    }

    @GetMapping("/list")
    public ApiSuccess<?> getNotificationList(@AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();
        List<NotificationDto> notificationDtoList = notificationService.getNotificationList(userId);
        return ApiUtil.success(notificationDtoList);
    }

    @PostMapping("/read")
    public ApiSuccess<?> updateNotificationAsRead(@RequestParam String uuid, @AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();
        notificationService.updateNotificationAsRead(userId, uuid);
        return ApiUtil.success("알림이 성공적으로 읽음 처리되었습니다.");
    }

    @PostMapping("/read-all")
    public ApiSuccess<?> markAllNotificationsAsRead(@AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();
        notificationService.notificationsAsRead(userId);
        return ApiUtil.success("모든 알림이 성공적으로 읽음 처리되었습니다.");
    }

    @PostMapping("/subscribe")
    public ApiSuccess<?> subscribe(@AuthenticationPrincipal CustomOAuth2User user, @RequestBody FcmTokenTopicRequestDto requestDto) {
        Long userId = user.getUserId();

        notificationService.saveOrUpdateFcmToken(userId, requestDto.getFcmToken());
        notificationTopicService.subscribeToTopic(requestDto.getFcmToken(), requestDto.getTopic());

        return ApiUtil.success("성공적으로 구독되었습니다.");
    }

    @PostMapping("/unsubscribe")
    public ApiSuccess<?> unsubscribe(@RequestBody FcmTokenTopicRequestDto requestDto) {

        notificationTopicService.unsubscribeFromTopic(requestDto.getFcmToken(), requestDto.getTopic());

        return ApiUtil.success("구독이 해제되었습니다.");
    }

    @PostMapping("/update/{enabled}")
    public ApiSuccess<?> updateUserNotification(@AuthenticationPrincipal CustomOAuth2User user, @PathVariable boolean enabled){
        Long userId = user.getUserId();
        notificationService.updateUserNotification(userId, enabled);
        return ApiUtil.success("알림 설정이 업데이트 되었습니다.");
    }
}