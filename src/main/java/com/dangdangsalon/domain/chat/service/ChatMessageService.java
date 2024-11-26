package com.dangdangsalon.domain.chat.service;

import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void saveMessageRedis(ChatMessageDto message) {
        String key = "chat:messages:" + message.getRoomId();
        redisTemplate.opsForList().rightPush(key, message);
        redisTemplate.expire(key, Duration.ofDays(1));
    }
}
