package com.dangdangsalon.domain.coupon.service;

import com.dangdangsalon.config.RedisPublisher;
import com.dangdangsalon.domain.coupon.dto.QueueStatusDto;
import com.dangdangsalon.domain.coupon.entity.Coupon;
import com.dangdangsalon.domain.coupon.entity.CouponEvent;
import com.dangdangsalon.domain.coupon.entity.CouponStatus;
import com.dangdangsalon.domain.coupon.repository.CouponEventRepository;
import com.dangdangsalon.domain.coupon.repository.CouponRepository;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponIssueService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisPublisher redisPublisher;
    private final CouponRepository couponRepository;
    private final CouponEventRepository couponEventRepository;
    private final UserRepository userRepository;

    private static final int MAX_QUEUE_SIZE = 1000;

    public String joinQueue(Long userId, Long eventId) {
        if (!isEventStarted(eventId)) {
            return "이벤트가 아직 시작되지 않았습니다.";
        }

        String issuedUsersKey = "issued_users:" + eventId;
        String queueKey = "coupon_queue:" + eventId;
        String couponRemainingKey = "coupon_remaining:" + eventId;

        // 남은 쿠폰 확인
        Integer remainingCoupons = (Integer) redisTemplate.opsForValue().get(couponRemainingKey);
        if (remainingCoupons == null || remainingCoupons <= 0) {
            return "쿠폰이 모두 소진되었습니다.";
        }

        Long queueLength = redisTemplate.opsForZSet().zCard(queueKey);
        if (queueLength >= MAX_QUEUE_SIZE) {
            return "쿠폰이 소진되었습니다. 다음 기회에 도전해주세요";
        }

        // 중복 체크: 이미 발급된 사용자
        Boolean isAlreadyIssued = redisTemplate.opsForSet().isMember(issuedUsersKey, userId.toString());
        if (isAlreadyIssued != null && isAlreadyIssued) {
            return "이미 쿠폰을 발급받았습니다.";
        }

        // 중복 체크: 이미 대기열에 있는 사용자
        Boolean isAlreadyInQueue = redisTemplate.opsForZSet().rank(queueKey, userId.toString()) != null;
        if (isAlreadyInQueue) {
            return "이미 대기열에 참여한 사용자입니다.";
        }

        redisTemplate.opsForZSet().add(queueKey, userId.toString(), System.currentTimeMillis());

        if (redisPublisher.isEmitterRegistered(userId)) {
            publishQueueStatus(eventId);
        } else {
            log.info("SSE 연결되지 않음, 대기열 상태 전송 생략: userId={}, eventId={}", userId, eventId);
        }

        return "대기열에 참여했습니다.";
    }

    public void publishQueueStatus(Long eventId) {
        String queueKey = "coupon_queue:" + eventId;
        String couponRemainingKey = "coupon_remaining:" + eventId;

        Long totalQueueLength = redisTemplate.opsForZSet().zCard(queueKey);
        Integer remainingCoupons = (Integer) redisTemplate.opsForValue().get(couponRemainingKey);

        if (totalQueueLength == null || remainingCoupons == null) {
            log.warn("대기열 데이터가 없습니다: eventId={}", eventId);
            return;
        }

        Set<Object> allUsers = redisTemplate.opsForZSet().range(queueKey, 0, -1);
        if (allUsers == null || allUsers.isEmpty()) {
            log.warn("대기열에 사용자가 없습니다: eventId={}", eventId);
            return;
        }

        // 모든 사용자에게 상태를 전송
        for (Object userIdObj : allUsers) {
            Long userId = Long.valueOf(userIdObj.toString());
            Long rank = redisTemplate.opsForZSet().rank(queueKey, userId.toString());
            if (rank == null) continue;

            int aheadCount = rank.intValue(); // 사용자 앞에 있는 사람 수
            int behindCount = totalQueueLength.intValue() - aheadCount - 1;

            // 예상 대기 시간 (초당 10명 처리 가정)
            int processingTimePerUser = 2; // 한 사용자 처리에 걸리는 평균 시간 (초)
            int estimatedTime = aheadCount * processingTimePerUser;

            // 사용자별로 데이터 생성
            QueueStatusDto queueStatus = QueueStatusDto.builder()
                    .eventId(eventId)
                    .queueLength(totalQueueLength.intValue())
                    .remainingCoupons(remainingCoupons)
                    .aheadCount(aheadCount)
                    .behindCount(behindCount)
                    .estimatedTime(estimatedTime)
                    .build();

            // 사용자별로 데이터 전송
            redisPublisher.sendToEmitter(userId, queueStatus, "queue_status");
            log.info("queueStatus for userId {}: ahead={}, behind={}, estimatedTime={}s", userId, aheadCount, behindCount, estimatedTime);
        }
    }

    public SseEmitter subscribeQueueUpdates(Long userId, Long eventId) {
        /*
         클라이언트에서 /queue/updates로 요청을 보내면 SseEmitter 객체를 생성해 SSE 연결을 만든다.
         SseEmitter는 HTTP 연결을 끊지 않고 유지하며 데이터를 스트리밍으로 전달.
         redisPublisher.registerEmitter를 통해 Pub/Sub 메시지를 받아 SseEmitter로 전달한다.
         따라서 실시간으로 대기열 상태를 수신할 수 있다.
         */

        String queueKey = "coupon_queue:" + eventId;
        Boolean isAlreadyInQueue = redisTemplate.opsForZSet().rank(queueKey, userId.toString()) != null;

        if (!isAlreadyInQueue) {
            throw new IllegalStateException("대기열에 참여하지 않은 사용자는 SSE 연결을 할 수 없습니다.");
        }

        SseEmitter emitter = new SseEmitter(-1L);
        redisPublisher.registerEmitter(userId, emitter);

        publishQueueStatus(eventId);

        return emitter;
    }

    @Transactional
    public void processQueue(Long eventId) {
        String queueKey = "coupon_queue:" + eventId;
        String couponRemainingKey = "coupon_remaining:" + eventId;
        String issuedUsersKey = "issued_users:" + eventId;

        while (true) {
            Set<Object> firstUserSet = redisTemplate.opsForZSet().range(queueKey, 0, 0);
            if (firstUserSet == null || firstUserSet.isEmpty()) {
                break;
            }
            String firstUser = (String) firstUserSet.iterator().next();

            // 남은 쿠폰 확인
            Integer remainingCoupons = (Integer) redisTemplate.opsForValue().get(couponRemainingKey);
            if (remainingCoupons == null || remainingCoupons <= 0) {
                publishQueueStatus(eventId);
                publishCouponIssueResult(false, Long.valueOf(firstUser));
                break;
            }

            // 쿠폰 발급 처리
            redisTemplate.opsForValue().decrement(couponRemainingKey);
            redisTemplate.opsForSet().add(issuedUsersKey, firstUser);
            redisTemplate.opsForZSet().remove(queueKey, firstUser);

            Boolean result = saveCouponToDatabase(Long.valueOf(firstUser), eventId);

            publishCouponIssueResult(result, Long.valueOf(firstUser));

            publishQueueStatus(eventId);
        }
    }

    public void initializeEvents() {
        List<CouponEvent> activeEvents = couponEventRepository.findActiveEvents(LocalDateTime.now());

        for (CouponEvent event : activeEvents) {
            String couponRemainingKey = "coupon_remaining:" + event.getId();
            String couponQueueKey = "coupon_queue:" + event.getId();
            String issuedUsersKey = "issued_users:" + event.getId();

            // Redis 초기화
            redisTemplate.opsForValue().set(couponRemainingKey, event.getTotalQuantity());
            redisTemplate.delete(couponQueueKey);
            redisTemplate.delete(issuedUsersKey);
        }
    }

    private Boolean saveCouponToDatabase(Long userId, Long eventId) {
        try {
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

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isEventStarted(Long eventId) {
        String couponRemainingKey = "coupon_remaining:" + eventId;
        return redisTemplate.hasKey(couponRemainingKey);
    }

    private void publishCouponIssueResult(Boolean result, Long userId) {
        redisPublisher.sendToEmitterAndClose(userId, result, "couponIssueResult");
    }

}
