package com.dangdangsalon.domain.chat.service;

import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String SAVE_MESSAGE_ROOM_ID_KEY = "chat:messages:";
    private static final String LAST_READ_KEY = "lastRead:";
    private static final String FIRST_LOADED_KEY = "firstLoadedIndex:";
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
        String firstLoadedKey = FIRST_LOADED_KEY + roomId + ":" + userId;

        Integer lastReadIndex = (Integer) redisTemplate.opsForValue().get(lastReadKey);
        Long totalMessageCount = redisTemplate.opsForList().size(messageKey);

        List<Object> rawMessages;

        if (lastReadIndex != null && totalMessageCount != null) {
            if (lastReadIndex >= totalMessageCount - 1) {
                int startIndex = Math.max(lastReadIndex - MESSAGE_GET_LIMIT + 1, 0);
                rawMessages = redisTemplate.opsForList().range(messageKey, startIndex, lastReadIndex);

                redisTemplate.opsForValue().set(firstLoadedKey, startIndex);
            } else {
                rawMessages = redisTemplate.opsForList().range(messageKey, lastReadIndex + 1, -1);

                redisTemplate.opsForValue().set(firstLoadedKey, lastReadIndex + 1);
            }
        } else {
            rawMessages = redisTemplate.opsForList().range(messageKey, -MESSAGE_GET_LIMIT, -1);

            if (totalMessageCount != null) {
                redisTemplate.opsForValue().set(firstLoadedKey, Math.max(totalMessageCount.intValue() - MESSAGE_GET_LIMIT, 0));
            }
        }

        updateLastReadMessage(messageKey, lastReadKey);

        return rawMessages.stream()
                .map(rawMessage -> objectMapper.convertValue(rawMessage, ChatMessageDto.class))
                .toList();
    }

    public List<ChatMessageDto> getPreviousMessages(Long roomId, Long userId) {
        String messageKey = SAVE_MESSAGE_ROOM_ID_KEY + roomId;
        String firstLoadedKey = FIRST_LOADED_KEY + roomId + ":" + userId;

        Integer firstLoadedIndex = (Integer) redisTemplate.opsForValue().get(firstLoadedKey);

        log.info("firstIndex = " + firstLoadedIndex);
        if (firstLoadedIndex == null || firstLoadedIndex <= 0) {
            return List.of();
        }

        int startIndex = Math.max(firstLoadedIndex - MESSAGE_GET_LIMIT, 0);
        int endIndex = firstLoadedIndex - 1;
        log.info("startIndex= " + startIndex + " endIndex= " + endIndex);
        List<Object> rawMessages = redisTemplate.opsForList().range(messageKey, startIndex, endIndex);

        redisTemplate.opsForValue().set(firstLoadedKey, startIndex);

        return rawMessages.stream()
                .map(rawMessage -> objectMapper.convertValue(rawMessage, ChatMessageDto.class))
                .toList();
    }

    public void deleteRedisData(Long roomId) {
        String messageKey = SAVE_MESSAGE_ROOM_ID_KEY + roomId;
        String lastReadKey = LAST_READ_KEY + roomId + ":*";
        String firstLoadedKey = FIRST_LOADED_KEY + roomId + ":*";

        redisTemplate.delete(messageKey);
        redisTemplate.delete(redisTemplate.keys(lastReadKey));
        redisTemplate.delete(redisTemplate.keys(firstLoadedKey));
    }

    private void updateLastReadMessage(String messageKey, String lastReadKey) {
        Long listSize = redisTemplate.opsForList().size(messageKey);
        if (listSize != null) {
            redisTemplate.opsForValue().set(lastReadKey, listSize.intValue() - 1);
        }
    }
}
