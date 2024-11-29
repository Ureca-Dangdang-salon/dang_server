package com.dangdangsalon.domain.chat.util;

import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatRedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String SAVE_MESSAGE_ROOM_ID_KEY = "chat:messages:";
    private static final String LAST_READ_KEY = "lastRead:";
    private static final String FIRST_LOADED_KEY = "firstLoadedIndex:";
    private static final int MESSAGE_GET_LIMIT = 7;

    public void saveMessage(ChatMessageDto message) {
        String key = SAVE_MESSAGE_ROOM_ID_KEY + message.getRoomId();

        redisTemplate.opsForList().rightPush(key, message);
        redisTemplate.expire(key, Duration.ofDays(1));
    }

    public void updateLastRead(ChatMessageDto message) {
        String key = SAVE_MESSAGE_ROOM_ID_KEY + message.getRoomId();
        String lastReadKey = LAST_READ_KEY + message.getRoomId() + ":" + message.getSenderId();

        Long listSize = redisTemplate.opsForList().size(key);

        if (listSize != null) {
            redisTemplate.opsForValue().set(lastReadKey, listSize.intValue());
        }
    }

    public Object getLastMessage(Long roomId) {
        String key = SAVE_MESSAGE_ROOM_ID_KEY + roomId;
        return redisTemplate.opsForList().index(key, -1);
    }

    public Long getTotalMessageCount(Long roomId) {
        String messageKey = SAVE_MESSAGE_ROOM_ID_KEY + roomId;
        return redisTemplate.opsForList().size(messageKey);
    }

    public Integer getLastReadIndex(Long roomId, Long userId) {
        String lastReadKey = LAST_READ_KEY + roomId + ":" + userId;
        return (Integer) redisTemplate.opsForValue().get(lastReadKey);
    }

    public void updateLastReadMessage(Long roomId, Long userId) {
        String lastReadKey = LAST_READ_KEY + roomId + ":" + userId;
        String messageKey = SAVE_MESSAGE_ROOM_ID_KEY + roomId;

        Long listSize = redisTemplate.opsForList().size(messageKey);
        if (listSize != null) {
            redisTemplate.opsForValue().set(lastReadKey, listSize.intValue() - 1);
        }
    }

    public List<Object> getMessagesForUnreadOrRecent(Long roomId, Integer lastReadIndex, Long totalMessageCount) {
        String messageKey = SAVE_MESSAGE_ROOM_ID_KEY + roomId;

        if (lastReadIndex != null && totalMessageCount != null) {
            if (lastReadIndex >= totalMessageCount - 1) {
                int startIndex = Math.max(lastReadIndex - MESSAGE_GET_LIMIT + 1, 0);
                return redisTemplate.opsForList().range(messageKey, startIndex, lastReadIndex);
            } else {
                return redisTemplate.opsForList().range(messageKey, lastReadIndex + 1, -1);
            }
        } else {
            return redisTemplate.opsForList().range(messageKey, -MESSAGE_GET_LIMIT, -1);
        }
    }


    public List<Object> getPreviousMessages(Long roomId, Long userId) {
        String messageKey = SAVE_MESSAGE_ROOM_ID_KEY + roomId;
        String firstLoadedKey = FIRST_LOADED_KEY + roomId + ":" + userId;

        Integer firstLoadedIndex = (Integer) redisTemplate.opsForValue().get(firstLoadedKey);

        if (firstLoadedIndex == null || firstLoadedIndex <= 0) {
            return List.of();
        }

        int startIndex = Math.max(firstLoadedIndex - MESSAGE_GET_LIMIT, 0);
        int endIndex = firstLoadedIndex - 1;

        List<Object> messages = redisTemplate.opsForList().range(messageKey, startIndex, endIndex);

        redisTemplate.opsForValue().set(firstLoadedKey, startIndex);

        return messages;
    }

    public void deleteRoomData(Long roomId) {
        String messageKey = SAVE_MESSAGE_ROOM_ID_KEY + roomId;
        String lastReadKey = LAST_READ_KEY + roomId + ":*";
        String firstLoadedKey = FIRST_LOADED_KEY + roomId + ":*";

        redisTemplate.delete(messageKey);
        redisTemplate.delete(redisTemplate.keys(lastReadKey));
        redisTemplate.delete(redisTemplate.keys(firstLoadedKey));
    }
}
