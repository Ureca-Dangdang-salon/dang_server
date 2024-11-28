package com.dangdangsalon.domain.chat.service;

import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
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
    private static final String LAST_READ_KEY = "lastRead";
    private static final int MESSAGE_GET_LIMIT = 7;

    public void saveMessageRedis(ChatMessageDto message) {
        String key = SAVE_MESSAGE_ROOM_ID_KEY + message.getRoomId();

        redisTemplate.opsForList().rightPush(key, message);
        redisTemplate.expire(key, Duration.ofDays(1));
    }

    public void updateLastReadKey(ChatMessageDto message) {
        String key = SAVE_MESSAGE_ROOM_ID_KEY + message.getRoomId();
        String lastReadKey = LAST_READ_KEY + message.getRoomId() + ":" + message.getSenderId();

        Long listSize = redisTemplate.opsForList().size(key);

        if (listSize != null) {
            redisTemplate.opsForValue().set(lastReadKey, listSize.intValue());
        }
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

    public int getUnreadCount(Long roomId, Long userId) {
        String lastReadKey = LAST_READ_KEY + roomId + ":" + userId;
        String messageKey = SAVE_MESSAGE_ROOM_ID_KEY + roomId;

        Integer lastReadIndex = (Integer) redisTemplate.opsForValue().get(lastReadKey);
        Long totalMessages = redisTemplate.opsForList().size(messageKey);

        if (totalMessages == null) {
            return 0;
        }

        if (lastReadIndex == null) {
            return totalMessages.intValue();
        }

        return Math.max(totalMessages.intValue() - (lastReadIndex + 1), 0);
    }

    public List<ChatMessageDto> getUnreadOrRecentMessages(Long roomId, Long userId) {
        String lastReadKey = LAST_READ_KEY + roomId + ":" + userId;
        String messageKey = SAVE_MESSAGE_ROOM_ID_KEY + roomId;

        Integer lastReadIndex = (Integer) redisTemplate.opsForValue().get(lastReadKey);

        List<Object> rawMessages = new ArrayList<>();

        if (lastReadIndex != null) {
            rawMessages = redisTemplate.opsForList()
                    .range(messageKey, Math.max(lastReadIndex - MESSAGE_GET_LIMIT + 1, 0), lastReadIndex);
        } else {
            rawMessages = redisTemplate.opsForList().range(messageKey, -MESSAGE_GET_LIMIT, -1);
        }

        List<ChatMessageDto> messages = rawMessages.stream()
                .map(rawMessage -> objectMapper.convertValue(rawMessage, ChatMessageDto.class))
                .toList();

        updateLastReadMessage(messageKey, lastReadKey);

        return messages;
    }

    public List<ChatMessageDto> getPreviousMessages(Long roomId, Long lastLoadedMessageId) {
        String messageKey = SAVE_MESSAGE_ROOM_ID_KEY + roomId;

        List<Object> rawMessages = redisTemplate.opsForList()
                .range(messageKey, lastLoadedMessageId - MESSAGE_GET_LIMIT, lastLoadedMessageId - 1);

        return rawMessages.stream()
                .map(rawMessage -> objectMapper.convertValue(rawMessage, ChatMessageDto.class))
                .toList();
    }

    public List<ChatMessageDto> getNextMessages(Long roomId, Long lastLoadedMessageId) {
        String messageKey = SAVE_MESSAGE_ROOM_ID_KEY + roomId;

        List<Object> rawMessages = redisTemplate.opsForList()
                .range(messageKey, lastLoadedMessageId + 1, lastLoadedMessageId + MESSAGE_GET_LIMIT);

        return rawMessages.stream()
                .map(rawMessage -> objectMapper.convertValue(rawMessage, ChatMessageDto.class))
                .toList();
    }

    private void updateLastReadMessage(String messageKey, String lastReadKey) {
        Long listSize = redisTemplate.opsForList().size(messageKey);
        if (listSize != null) {
            redisTemplate.opsForValue().set(lastReadKey, listSize.intValue() - 1);
        }
    }
}
