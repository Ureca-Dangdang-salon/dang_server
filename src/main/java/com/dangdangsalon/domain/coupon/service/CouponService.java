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
import java.util.Set;
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
        String queueKey = "coupon_queue:" + eventId;
        String couponRemainingKey = "coupon_remaining:" + eventId;

        // 남은 쿠폰 확인
        Integer remainingCoupons = (Integer) redisTemplate.opsForValue().get(couponRemainingKey);
        if (remainingCoupons == null || remainingCoupons <= 0) {
            throw new IllegalStateException("쿠폰이 모두 소진되었습니다.");
        }

        // 중복 체크: 이미 발급된 사용자
        Boolean isAlreadyIssued = redisTemplate.opsForSet().isMember(issuedUsersKey, userId.toString());
        if (isAlreadyIssued != null && isAlreadyIssued) {
            throw new IllegalStateException("이미 쿠폰을 발급받았습니다. Id= " + userId);
        }

        // 중복 체크: 이미 대기열에 있는 사용자
        Boolean isAlreadyInQueue = redisTemplate.opsForZSet().rank(queueKey, userId.toString()) != null;
        if (isAlreadyInQueue) {
            throw new IllegalStateException("이미 대기열에 참여한 사용자입니다.");
        }

        // ZSet에 사용자 추가 (타임스탬프를 score로 사용)
        redisTemplate.opsForZSet().add(queueKey, userId.toString(), System.currentTimeMillis());

        // 대기열 상태 갱신
        publishQueueStatus(eventId, userId);
    }

    public void publishQueueStatus(Long eventId, Long userId) {
        String queueKey = "coupon_queue:" + eventId;
        String couponRemainingKey = "coupon_remaining:" + eventId;

        // 사용자 순위 및 대기열 상태 계산
        Long rank = redisTemplate.opsForZSet().rank(queueKey, userId.toString());
        log.info("rank= " + rank);
        Long totalQueueLength = redisTemplate.opsForZSet().zCard(queueKey);
        Integer remainingCoupons = (Integer) redisTemplate.opsForValue().get(couponRemainingKey);

        int aheadCount = rank != null ? rank.intValue() : 0;
        int behindCount = totalQueueLength != null && rank != null ? totalQueueLength.intValue() - aheadCount - 1 : 0;

        // 예상 대기 시간 (초당 10명 처리 가정)
        int processingTimePerUser = 10; // 한 사용자 처리에 걸리는 평균 시간 (초)
        int estimatedTime = aheadCount * processingTimePerUser;

        QueueStatusDto queueStatus = QueueStatusDto.builder()
                .eventId(eventId)
                .queueLength(totalQueueLength != null ? totalQueueLength.intValue() : 0)
                .remainingCoupons(remainingCoupons != null ? remainingCoupons : 0)
                .aheadCount(aheadCount)
                .behindCount(behindCount)
                .estimatedTime(estimatedTime)
                .build();

        log.info("queueStatus: ahead={}, behind={}, estimatedTime={}s", aheadCount, behindCount, estimatedTime);
        redisPublisher.publish("queue_status", queueStatus);
    }

    public SseEmitter subscribeQueueUpdates() {
        /*
         클라이언트에서 /queue/updates로 요청을 보내면 SseEmitter 객체를 생성해 SSE 연결을 만든다.
         SseEmitter는 HTTP 연결을 끊지 않고 유지하며 데이터를 스트리밍으로 전달.
         redisPublisher.registerEmitter를 통해 Pub/Sub 메시지를 받아 SseEmitter로 전달한다.
         따라서 실시간으로 대기열 상태를 수신할 수 있다.
         */
        log.info("SSE Emitter 생성");
        SseEmitter emitter = new SseEmitter(0L); // 무제한 연결 유지
        redisPublisher.registerEmitter(emitter);

        try {
            // SSE 초기 상태를 전송
            String couponQueueKey = "coupon_queue:" + 1L;
            String couponRemainingKey = "coupon_remaining:" + 1L;

            Long queueLength = redisTemplate.opsForZSet().zCard(couponQueueKey);
            Integer remainingCoupons = (Integer) redisTemplate.opsForValue().get(couponRemainingKey);

            QueueStatusDto initialStatus = QueueStatusDto.builder()
                    .eventId(1L)
                    .queueLength(queueLength != null ? queueLength.intValue() : 0)
                    .remainingCoupons(remainingCoupons != null ? remainingCoupons : 0)
                    .aheadCount(0) // 초기 상태에서는 앞에 아무도 없으므로 0
                    .behindCount(0) // 초기 상태에서는 뒤에 아무도 없으므로 0
                    .estimatedTime(0) // 초기 상태에서 예상 시간은 0
                    .build();

            log.info("SSE 초기 상태 전송: {}", initialStatus);
            emitter.send(SseEmitter.event().name("queueStatus").data(initialStatus));
        } catch (Exception e) {
            log.error("SSE 초기 상태 전송 중 오류 발생", e);
        }

        log.info("SSE Emitter 등록 완료");
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
                publishQueueStatus(eventId, Long.valueOf(firstUser));
                break;
            }

            // 쿠폰 발급 처리
            redisTemplate.opsForValue().decrement(couponRemainingKey);
            redisTemplate.opsForSet().add(issuedUsersKey, firstUser);
            redisTemplate.opsForZSet().remove(queueKey, firstUser);

            saveCouponToDatabase(Long.valueOf(firstUser), eventId);

            publishQueueStatus(eventId, Long.valueOf(firstUser));
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
