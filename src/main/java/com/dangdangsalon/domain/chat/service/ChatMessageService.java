package com.dangdangsalon.domain.chat.service;

import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String SAVE_MESSAGE_ROOM_ID_KEY = "chat:messages:";

    public void saveMessageRedis(ChatMessageDto message) {
        String key = SAVE_MESSAGE_ROOM_ID_KEY + message.getRoomId();
        redisTemplate.opsForList().rightPush(key, message);
        redisTemplate.expire(key, Duration.ofDays(1));
    }

    public String getLastMessage(Long roomId) {
        String key = SAVE_MESSAGE_ROOM_ID_KEY + roomId;
        Object lastMessage = redisTemplate.opsForList().index(key, -1);

        if (lastMessage instanceof ChatMessageDto) {
            return ((ChatMessageDto) lastMessage).getMessageText();
        }

        if (lastMessage instanceof LinkedHashMap) {
            return objectMapper.convertValue(lastMessage, ChatMessageDto.class).getMessageText();
        }

        throw new IllegalStateException("데이터가 올바르지 않습니다.");
    }

    public int getUnreadCount(Long roomId) {
        return 3;
    }

    public List<ChatMessageDto> getRecentMessages(Long roomId) {
        String key = SAVE_MESSAGE_ROOM_ID_KEY + roomId;

        //일단 최신 10개의 메시지만 가져온다.
        List<Object> rawMessages = redisTemplate.opsForList().range(key, -10, -1);

        if (rawMessages.isEmpty()) {
            return List.of();
        }

        return rawMessages.stream()
                .map(rawMessage -> objectMapper.convertValue(rawMessage, ChatMessageDto.class))
                .toList();
    }
}
