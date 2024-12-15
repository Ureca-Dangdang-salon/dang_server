//package com.dangdangsalon.domain.coupon.listener;
//
//import com.dangdangsalon.config.RedisPublisher;
//import com.dangdangsalon.domain.coupon.dto.QueueStatusDto;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.connection.Message;
//import org.springframework.data.redis.connection.MessageListener;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class RedisMessageSubscriber implements MessageListener {
//
//    private final RedisPublisher redisPublisher;
//    private final ObjectMapper objectMapper;
//
//    @Override
//    public void onMessage(Message message, byte[] pattern) {
//        try {
//            String messageBody = new String(message.getBody());
//            String actualJson = objectMapper.readValue(messageBody, String.class);
//
//            String channel = new String(message.getChannel());
//
//            log.info("channel: " + channel + " " + "actualJson: " + actualJson);
//
//            if ("queue_status".equals(channel)) {
//                log.info("queue message process");
//                processQueueStatusMessage(actualJson);
//            }
//        } catch (Exception e) {
//            log.error("Redis 수신 메시지 처리 중 오류 발생", e);
//        }
//    }
//
//    private void processQueueStatusMessage(String actualJson) throws JsonProcessingException {
//        QueueStatusDto queueStatus = objectMapper.convertValue(
//                objectMapper.readTree(actualJson), QueueStatusDto.class
//        );
//
//        log.info("queue message process msg= " + queueStatus.getQueueLength());
//        redisPublisher.broadcast(queueStatus, "queueStatus");
//    }
//}
