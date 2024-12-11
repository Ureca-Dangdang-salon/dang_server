package com.dangdangsalon.domain.coupon.service;

import com.dangdangsalon.config.RedisPublisher;
import com.dangdangsalon.domain.coupon.dto.QueueStatusDto;
import com.dangdangsalon.domain.coupon.entity.Coupon;
import com.dangdangsalon.domain.coupon.entity.CouponEvent;
import com.dangdangsalon.domain.coupon.entity.CouponStatus;
import com.dangdangsalon.domain.coupon.entity.DiscountType;
import com.dangdangsalon.domain.coupon.repository.CouponEventRepository;
import com.dangdangsalon.domain.coupon.repository.CouponRepository;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisPublisher redisPublisher;
    private final CouponRepository couponRepository;
    private final CouponEventRepository couponEventRepository;
    private final UserRepository userRepository;

    public void joinQueue(Long userId, Long eventId) {
        if (!isEventStarted(eventId)) {
            throw new IllegalStateException("이벤트가 아직 시작되지 않았습니다.");
        }

        String issuedUsersKey = "issued_users:" + eventId;
        String couponQueueKey = "coupon_queue:" + eventId;

        Boolean isAlreadyIssued = redisTemplate.opsForSet().isMember(issuedUsersKey, userId);
        if (isAlreadyIssued != null && isAlreadyIssued) {
            throw new IllegalStateException("이미 쿠폰을 발급받았습니다.");
        }

        redisTemplate.opsForList().rightPush(couponQueueKey, userId);

        publishQueueStatus(eventId);
    }

    public void publishQueueStatus(Long eventId) {
        String couponQueueKey = "coupon_queue:" + eventId;
        String couponRemainingKey = "coupon_remaining:" + eventId;

        Long queueLength = redisTemplate.opsForList().size(couponQueueKey);
        Integer remainingCoupons = (Integer) redisTemplate.opsForValue().get(couponRemainingKey);

        QueueStatusDto queueStatus = QueueStatusDto.builder()
                .eventId(eventId)
                .queueLength(queueLength != null ? queueLength : 0)
                .remainingCoupons(remainingCoupons != null ? remainingCoupons : 0)
                .build();

        redisPublisher.publish("queue_status:" + eventId, queueStatus); // Pub/Sub 채널로 대기열 상태 메시지 발행
    }

    public SseEmitter subscribeQueueUpdates() {
        /*
         클라이언트에서 /queue/updates로 요청을 보내면 SseEmitter 객체를 생성해 SSE 연결을 만든다.
         SseEmitter는 HTTP 연결을 끊지 않고 유지하며 데이터를 스트리밍으로 전달.
         redisPublisher.registerEmitter를 통해 Pub/Sub 메시지를 받아 SseEmitter로 전달한다.
         따라서 실시간으로 대기열 상태를 수신할 수 있다.
         */
        SseEmitter emitter = new SseEmitter(0L); // 무제한 연결 유지
        redisPublisher.registerEmitter(emitter);
        return emitter;
    }

    // 스케줄러 활용하여 주기적으로 대기열을 처리하도록 구현.
    @Transactional
    public void processQueue(Long eventId) {
        String couponQueueKey = "coupon_queue:" + eventId;
        String couponRemainingKey = "coupon_remaining:" + eventId;
        String issuedUsersKey = "issued_users:" + eventId;

        while (true) {
            Integer userId = (Integer) redisTemplate.opsForList().leftPop(couponQueueKey);
            if (userId == null) {
                break;
            }

            Integer remainingCoupons = (Integer) redisTemplate.opsForValue().get(couponRemainingKey);
            if (remainingCoupons == null || remainingCoupons <= 0) {
                publishQueueStatus(eventId);
                break;
                //쿠폰 매진 정보를 알려 대기열에서 의미없는 대기 하지 않도록 구현.
            }

            redisTemplate.opsForValue().decrement(couponRemainingKey);
            redisTemplate.opsForSet().add(issuedUsersKey, userId);

            saveCouponToDatabase(Long.valueOf(userId), eventId);

            publishQueueStatus(eventId);
        }
    }

    private void saveCouponToDatabase(Long userId, Long eventId) {
        CouponEvent couponEvent = couponEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("해당 이벤트를 찾을 수 없습니다. Id: " + eventId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("해당 사용자를 찾을 수 없습니다. Id: " + userId));

        Coupon coupon = Coupon.builder()
                .couponName(couponEvent.getName())
                .discountAmount(couponEvent.getDiscountAmount())
                .discountType(couponEvent.getDiscountType())
                .expiredAt(LocalDateTime.now().plusDays(30))
                .status(CouponStatus.NOT_USED)
                .user(user)
                .build();

        couponRepository.save(coupon);
    }

    private boolean isEventStarted(Long eventId) {
        String couponRemainingKey = "coupon_remaining:" + eventId;

        return redisTemplate.hasKey(couponRemainingKey);
    }
}
