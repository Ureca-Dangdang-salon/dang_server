package com.dangdangsalon.domain.coupon.listener;

import com.dangdangsalon.config.RedisPublisher;
import com.dangdangsalon.domain.coupon.dto.QueueStatusDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisMessageSubscriber implements MessageListener {

    private final RedisPublisher redisPublisher;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String messageBody = new String(message.getBody());
            String actualJson = objectMapper.readValue(messageBody, String.class);
            log.info("Redis Pub/Sub 메시지 수신: {}", messageBody);

            QueueStatusDto queueStatus = objectMapper.convertValue(
                    objectMapper.readTree(actualJson), QueueStatusDto.class
            );

            // 수신한 모든 메시지를 SSE에 전달해 클라이언트에 실시간으로 알림이 가능
            redisPublisher.sendToEmitters(queueStatus);
        } catch (Exception e) {
            log.error("Redis 수신 메시지 처리 중 오류 발생", e);
        }
    }
}
