package com.dangdangsalon.config;

import com.dangdangsalon.domain.coupon.dto.QueueStatusDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisPublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void publish(String channel, Object message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            redisTemplate.convertAndSend(channel, jsonMessage);
        } catch (Exception e) {
            log.error("Redis 메시지 발행 중 오류 발생", e);
        }
    }

    public void registerEmitter(Long userId, SseEmitter emitter) {
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));
    }

    public void sendToEmitters(Long userId, QueueStatusDto message) {
        SseEmitter emitter = emitters.get(userId);

        try {
            emitter.send(SseEmitter.event().name("queueStatus").data(message));
        } catch (IOException e) {
            emitters.remove(userId);
        }
    }

    public void sendToEmitter(Long userId, QueueStatusDto queueStatus, String eventName) {
        SseEmitter emitter = emitters.get(userId);

        if (emitter == null) {
            log.warn("SSE Emitter를 찾을 수 없음: userId={}", userId);
            return;
        }

        try {
            emitter.send(SseEmitter.event().name(eventName).data(queueStatus));
            log.info("SSE 데이터 전송 성공: userId={}, eventName={}, data={}", userId, eventName, queueStatus);
        } catch (IOException e) {
            log.error("SSE 데이터 전송 중 오류 발생: userId={}, 오류={}", userId, e.getMessage(), e);
            emitters.remove(userId);
        }
    }

    public void sendToEmitterAndClose(Long userId, Object message, String eventName) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) {
            log.warn("SSE Emitter를 찾을 수 없음: userId={}", userId);
            return;
        }

        try {
            emitter.send(SseEmitter.event().name(eventName).data(message));
        } catch (IOException e) {
            log.error("SSE 전송 중 오류 발생: userId={}, 오류={}", userId, e.getMessage(), e);
        } finally {
            emitter.complete(); // 연결 종료
            emitters.remove(userId);
        }
    }

    public void broadcast(Object message, String eventName) {
        emitters.forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(message));
            } catch (IOException e) {
                emitters.remove(userId);
                log.error("SSE 브로드캐스트 중 오류 발생: userId={}, 오류={}", userId, e.getMessage(), e);
            }
        });
    }

    public boolean isEmitterRegistered(Long userId) {
        return emitters.containsKey(userId);
    }
}
