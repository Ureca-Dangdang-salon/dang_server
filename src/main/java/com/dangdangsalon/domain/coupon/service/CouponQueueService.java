package com.dangdangsalon.domain.coupon.service;

import com.dangdangsalon.domain.coupon.dto.QueueStatusResponse;
import com.dangdangsalon.domain.coupon.repository.ZSetRedisRepository;
import com.dangdangsalon.domain.coupon.sse.SseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CouponQueueService {

    private static final int QUEUE_EXPIRATION_DAYS = 7;
    private static final String ISSUED_KEY_FORMAT = "coupon:event:%s:issued:%d";
    private static final String QUEUE_KEY_FORMAT = "coupon:event:%s:queue";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ZSetRedisRepository zSetRedisRepository;
    private final SseEmitterRegistry sseEmitterRegistry;
    private final ChannelTopic queueTopic;

    /**
     * 대기열에 사용자 추가
     */
    public void addToQueue(String eventName, Long memberId, double registerTime) {
        String queueKey = String.format(QUEUE_KEY_FORMAT, eventName);
        zSetRedisRepository.addIfAbsent(queueKey, memberId, registerTime, QUEUE_EXPIRATION_DAYS);

        // Redis Pub/Sub 메시지 발행
        redisTemplate.convertAndSend(queueTopic.getTopic(), eventName);
    }

    /**
     * 사용자 발급 기록 추가
     */
    public void markCouponAsIssued(String eventName, Long memberId) {
        String issuedKey = String.format(ISSUED_KEY_FORMAT, eventName, memberId);
        ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
        valueOps.set(issuedKey, true); // Redis에 기록
        redisTemplate.expire(issuedKey, Duration.ofDays(QUEUE_EXPIRATION_DAYS)); // 7일 후 자동 만료
    }

    /**
     * 큐 상태로 실시간 상태창 정보 요청
     */
    public void streamQueueStatus(String eventName, Long userId) {
        String key = String.format(QUEUE_KEY_FORMAT, eventName);

        Long rank = zSetRedisRepository.rank(key, userId);

        if (rank == null) {
            // 사용자 대기열에 없음
            sendEvent(userId, QueueStatusResponse.ofEmptyQueue(eventName));
            return;
        }

        long queueSize = zSetRedisRepository.size(key);
        long remainingAhead = rank;
        long remainingBehind = queueSize - rank - 1;
        long estimatedSeconds = remainingAhead * 10;

        QueueStatusResponse response = new QueueStatusResponse(
                eventName,
                queueSize,
                rank + 1,
                remainingAhead,
                remainingBehind,
                String.format("%02d:%02d", estimatedSeconds / 60, estimatedSeconds % 60),
                queueSize == 0 ? 100 : (int) ((queueSize - rank) * 100 / queueSize),
                false
        );

        sendEvent(userId, response);
    }

    /**
     * 클라이언트에 실시간 상태창 전달
     */
    public void sendEvent(Long userId, QueueStatusResponse response) {
        SseEmitter emitter = sseEmitterRegistry.get(userId);

        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("queue-status")
                        .data(response));
            } catch (Exception e) {
                sseEmitterRegistry.remove(userId);
                System.err.println("SSE 전송 실패: " + e.getMessage());
            }
        }
    }

    /**
     * 발급 요청 즉시 모든 유저에게 실시간 상태창 정보 전달
     */
    public void notifyAllUsers(String eventName) {
        String key = String.format(QUEUE_KEY_FORMAT, eventName);

        // Redis에서 대기열에 있는 모든 사용자 가져오기
        Set<Long> userIds = zSetRedisRepository.range(key, 0, -1);

        // 각 사용자에게 대기열 상태 전송
        for (Long userId : userIds) {
            streamQueueStatus(eventName, userId);
        }
    }
}
