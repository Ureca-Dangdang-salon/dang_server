package com.dangdangsalon.domain.chat.util;

import static com.dangdangsalon.domain.chat.util.ChatConst.*;
import static com.dangdangsalon.domain.chat.util.ChatRedisConfig.*;

import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import com.dangdangsalon.util.RedisKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatRedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatRedisConfig redisConfig;

    public void saveMessage(ChatMessageDto message) {
        String key = redisConfig.getSaveMessageKey(message.getRoomId());

        redisTemplate.opsForList().rightPush(key, message);
        redisTemplate.expire(key, TTL);
    }

    public void updateLastRead(ChatMessageDto message) {
        String key = redisConfig.getSaveMessageKey(message.getRoomId());
        String lastReadKey = redisConfig.getLastReadKey(message.getRoomId(), message.getSenderId());

        Long listSize = redisTemplate.opsForList().size(key);

        if (listSize != null) {
            redisTemplate.opsForValue().set(lastReadKey, listSize.intValue());
        }
    }

    public Object getLastMessage(Long roomId) {
        String key = redisConfig.getSaveMessageKey(roomId);
        return redisTemplate.opsForList().index(key, -1);
    }

    public Long getTotalMessageCount(Long roomId) {
        String messageKey = redisConfig.getSaveMessageKey(roomId);
        return redisTemplate.opsForList().size(messageKey);
    }

    public Integer getLastReadIndex(Long roomId, Long userId) {
        String lastReadKey = redisConfig.getLastReadKey(roomId, userId);
        return (Integer) redisTemplate.opsForValue().get(lastReadKey);
    }

    public void updateLastReadMessage(Long roomId, Long userId) {
        String messageKey = redisConfig.getSaveMessageKey(roomId);
        String lastReadKey = redisConfig.getLastReadKey(roomId, userId);

        Long listSize = redisTemplate.opsForList().size(messageKey);
        if (listSize != null) {
            redisTemplate.opsForValue().set(lastReadKey, listSize.intValue() - 1);
        }
    }

    public List<Object> getMessagesForUnreadOrRecent(Long roomId, Integer lastReadIndex, Long totalMessageCount) {
        String messageKey = redisConfig.getSaveMessageKey(roomId);

        if (lastReadIndex != null && totalMessageCount != null) {
            if (lastReadIndex >= totalMessageCount - 1) {
                int startIndex = Math.max(lastReadIndex - MESSAGE_GET_LIMIT.getCount() + 1, 0);
                return redisTemplate.opsForList().range(messageKey, startIndex, lastReadIndex);
            } else {
                return redisTemplate.opsForList().range(messageKey, lastReadIndex + 1, -1);
            }
        } else {
            return redisTemplate.opsForList().range(messageKey, -MESSAGE_GET_LIMIT.getCount(), -1);
        }
    }


    public List<Object> getPreviousMessages(Long roomId, Long userId) {
        String messageKey = redisConfig.getSaveMessageKey(roomId);
        String firstLoadedKey = redisConfig.getFirstLoadedKey(roomId, userId);

        Integer firstLoadedIndex = (Integer) redisTemplate.opsForValue().get(firstLoadedKey);

        if (firstLoadedIndex == null || firstLoadedIndex <= 0) {
            return List.of();
        }

        int startIndex = Math.max(firstLoadedIndex - MESSAGE_GET_LIMIT.getCount(), 0);
        int endIndex = firstLoadedIndex - 1;

        List<Object> messages = redisTemplate.opsForList().range(messageKey, startIndex, endIndex);

        redisTemplate.opsForValue().set(firstLoadedKey, startIndex);

        return messages;
    }

    public void deleteRoomData(Long roomId) {
        String messageKey = redisConfig.getSaveMessageKey(roomId);
        String lastReadKey = redisConfig.getRoomLastReadKey(roomId);
        String firstLoadedKey = redisConfig.getRoomFirstLoadedKey(roomId);

        redisTemplate.delete(messageKey);
        redisTemplate.delete(redisTemplate.keys(lastReadKey));
        redisTemplate.delete(redisTemplate.keys(firstLoadedKey));
    }

    public Object getMessageAtIndex(Long roomId, int index) {
        String messageKey = redisConfig.getSaveMessageKey(roomId);

        return redisTemplate.opsForList().index(messageKey, index);
    }

    public Integer getFirstLoadedIndex(Long roomId, Long userId) {
        String firstLoadedKey = redisConfig.getFirstLoadedKey(roomId, userId);
        return (Integer) redisTemplate.opsForValue().get(firstLoadedKey);
    }

    public Object getLastReadMessage(Long roomId, Integer lastReadIndex) {
        String messageKey = redisConfig.getSaveMessageKey(roomId);
        return redisTemplate.opsForList().index(messageKey, lastReadIndex);
    }

    public List<Object> getAllMessagesFromRoom(String roomKey) {
        return redisTemplate.opsForList().range(roomKey, 0, -1);
    }

    public List<String> getAllRoomKeys() {
        Set<String> keys = redisTemplate.keys(redisConfig.getAllSaveMessageKey());

        if (keys == null) {
            return List.of();
        }

        return List.copyOf(keys);
    }

    public Long extractRoomIdFromKey(String roomKey) {
        String prefix = RedisKey.SAVE_MESSAGE_ROOM_ID_KEY.getKey();
        return Long.parseLong(roomKey.replace(prefix, ""));
    }

    public void updateFirstLoadedIndex(Long roomId, Long userId, int index) {
        String firstLoadedKey = redisConfig.getFirstLoadedKey(roomId, userId);
        redisTemplate.opsForValue().set(firstLoadedKey, index);
    }
}
