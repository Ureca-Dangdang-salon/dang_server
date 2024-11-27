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

    private static final String SAVE_MESSAGE_ROOM_ID_KEY = "chat:messages:";

    public void saveMessageRedis(ChatMessageDto message) {
        String key = SAVE_MESSAGE_ROOM_ID_KEY + message.getRoomId();
        redisTemplate.opsForList().rightPush(key, message);
        redisTemplate.expire(key, Duration.ofDays(1));
    }

    public String getLastMessage(Long roomId) {
        String key = SAVE_MESSAGE_ROOM_ID_KEY + roomId;
        Object lastMessage = redisTemplate.opsForList().index(key, -1);

        return (String) lastMessage;
    }

    public int getUnreadCount(Long roomId) {
        return 3;
    }
}
