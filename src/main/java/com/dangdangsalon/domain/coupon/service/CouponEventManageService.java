package com.dangdangsalon.domain.coupon.service;

import com.dangdangsalon.domain.coupon.entity.Coupon;
import com.dangdangsalon.domain.coupon.entity.CouponStatus;
import com.dangdangsalon.domain.coupon.entity.DiscountType;
import com.dangdangsalon.domain.coupon.repository.CouponRepository;
import com.dangdangsalon.domain.user.entity.User;
import com.dangdangsalon.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponEventManageService {

    private static final String EVENT_QUEUE_KEY = "coupon:event:%s:queue";
    private static final String EVENT_REMAIN_KEY = "coupon:event:%s:remaining";
    private static final String EVENT_ISSUED_KEY = "coupon:event:%s:issued";
    private final CouponQueueService couponQueueService;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public void issueCoupons(String eventName) {
        String queueKey = String.format(EVENT_QUEUE_KEY, eventName);
        String remainKey = String.format(EVENT_REMAIN_KEY, eventName);
        String issuedKey = String.format(EVENT_ISSUED_KEY, eventName);

        // Lua 스크립트로 원자적 작업 수행
        String luaScript =
                "local userId = redis.call('ZRANGE', KEYS[1], 0, 0)[1] " +
                        "if userId then " +
                        "  redis.call('ZREM', KEYS[1], userId) " +
                        "  local remaining = redis.call('DECR', KEYS[2]) " +
                        "  if remaining >= 0 then " +
                        "    redis.call('SADD', KEYS[3], userId) " +
                        "    return userId " +
                        "  else " +
                        "    redis.call('INCR', KEYS[2]) " +
                        "    return nil " +
                        "  end " +
                        "else " +
                        "  return nil " +
                        "end";

        Long userId = redisTemplate.execute((RedisCallback<Long>) connection -> {
            byte[] result = (byte[]) connection.scriptingCommands()
                    .eval(luaScript.getBytes(), ReturnType.VALUE, 3,
                            queueKey.getBytes(), remainKey.getBytes(), issuedKey.getBytes());
            return result != null ? Long.parseLong(new String(result)) : null;
        });

        if (userId == null) {
            System.out.println("쿠폰 발급 실패: 대기열이 비었습니다.");
            return;
        }

        asyncCouponProcessing(eventName, userId);
    }

    @Async
    public void asyncCouponProcessing(String eventName, Long userId) {
        String remainingKey = String.format("coupon:event:%s:remaining", eventName);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId: " + userId));

        try {
            // 쿠폰 발급 데이터 저장
            Coupon coupon = Coupon.builder()
                    .expiredAt(LocalDateTime.now().plusDays(7))
                    .status(CouponStatus.NOT_USED)
                    .couponName(eventName)
                    .discountAmount(5000) // 예제 금액
                    .discountType(DiscountType.FIXED) // 예제 타입
                    .user(user)
                    .build();
            couponRepository.save(coupon);

            // 발급 기록에 사용자 추가
            couponQueueService.markCouponAsIssued(eventName, userId);

            System.out.println("쿠폰 발급 성공: userId: " + userId);
        } catch (Exception e) {
            System.err.println("쿠폰 발급 처리 중 오류: userId: " + userId + ", 이유: " + e.getMessage());
            // 실패 시 Redis에서 수량 복구
            redisTemplate.opsForValue().increment(remainingKey);
        }
    }

    public String registerUserForEvent(String eventName, Long userId) {
        String queueKey = String.format(EVENT_QUEUE_KEY, eventName);
        String remainKey = String.format(EVENT_REMAIN_KEY, eventName);
        String issuedKey = String.format(EVENT_ISSUED_KEY, eventName);

        Long remaining = redisTemplate.opsForValue().getOperations().boundValueOps(remainKey).increment(0);
        if (remaining == null || remaining <= 0) {
            return "이벤트 쿠폰의 개수가 모두 소진되었습니다.";
        }

        boolean alreadyIssued = Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(issuedKey, userId.toString()));

        if (alreadyIssued) {
            return "이미 쿠폰이 발급된 사용자입니다.";
        }

        boolean alreadyInQueue = redisTemplate.opsForZSet().score(queueKey, userId.toString()) != null;
        if (alreadyInQueue) {
            return "이미 대기열에 등록된 사용자입니다.";
        }

        couponQueueService.addToQueue(eventName, userId, System.currentTimeMillis());
        return "대기열 등록이 완료되었습니다.";
    }
}

