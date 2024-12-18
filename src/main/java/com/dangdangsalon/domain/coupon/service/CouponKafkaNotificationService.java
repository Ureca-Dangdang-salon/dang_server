package com.dangdangsalon.domain.coupon.service;

import com.dangdangsalon.domain.coupon.entity.CouponEvent;
import com.dangdangsalon.domain.coupon.repository.CouponEventRepository;
import com.dangdangsalon.domain.notification.dto.EventNotificationDto;
import com.dangdangsalon.domain.notification.entity.FcmToken;
import com.dangdangsalon.domain.notification.repository.FcmTokenRepository;
import com.dangdangsalon.domain.notification.service.EventNotificationProducer;
import com.dangdangsalon.domain.user.entity.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponKafkaNotificationService {

    private final CouponEventRepository couponEventRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final EventNotificationProducer producer;

    @Scheduled(cron = "0 40 15 * * *")
    public void sendCouponNotifications() {
        log.info("스케줄러 시작");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);

        // 1시간 이내에 시작하고, 아직 종료되지 않은 단일 이벤트 조회
        CouponEvent upcomingEvent = couponEventRepository.findFirstByStartedAtBetweenAndEndedAtAfter(
                now.minusSeconds(1), oneHourLater, now);

        if (upcomingEvent == null) {
            log.info("현재 1시간 이내에 시작하고 종료되지 않은 이벤트가 없습니다.");
            return;
        }

        log.info("조회된 이벤트: {}", upcomingEvent);

        // FCM 토큰
        List<String> fcmTokens = fcmTokenRepository.findAllByUserRole(Role.ROLE_USER);


        if (fcmTokens.isEmpty()) {
            log.warn("알림을 전송할 FCM 토큰이 없습니다.");
            return;
        }

        // 이벤트 알림 생성 및 전송
        String formattedStartTime = upcomingEvent.getStartedAt().toLocalTime().toString();
        String message = String.format("'%s' 이벤트가 1시간 후에 시작됩니다! 시작 시간: %s", upcomingEvent.getName(), formattedStartTime);

        EventNotificationDto batchNotification = EventNotificationDto.builder()
                .fcmToken(fcmTokens)
                .title("쿠폰 이벤트 알림")
                .message(message)
                .referenceId(upcomingEvent.getId())
                .build();

        producer.sendEventNotification(batchNotification);
        log.info("이벤트 '{}'에 대한 알림을 모든 사용자에게 일괄 전송했습니다.", upcomingEvent.getName());
    }
}