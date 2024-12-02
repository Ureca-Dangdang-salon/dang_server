package com.dangdangsalon.domain.chat.util;

import static com.dangdangsalon.domain.chat.util.ChatConst.*;
import static com.dangdangsalon.domain.chat.util.ChatRedisConfig.*;

import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import com.dangdangsalon.util.RedisKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
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

        redisTemplate.opsForZSet().add(key, message, message.getSequence());
        redisTemplate.expire(key, TTL);
    }

    public Long getNextSequence(Long roomId) {
        String sequenceKey = redisConfig.getSequenceKey(roomId);
        return redisTemplate.opsForValue().increment(sequenceKey);
    }

    public Long getCurrentSequence(Long roomId) {
        String sequenceKey = redisConfig.getSequenceKey(roomId);
        Object sequence = redisTemplate.opsForValue().get(sequenceKey);

        if (sequence == null) {
            return 0L;
        }

        return Long.parseLong(sequence.toString());
    }

    public List<Object> getMessagesBySequence(Long roomId, Long startSequence, Long endSequence) {
        String key = redisConfig.getSaveMessageKey(roomId);
        return Objects.requireNonNull(redisTemplate.opsForZSet()
                        .rangeByScore(key, startSequence, endSequence))
                .stream()
                .toList();
    }

    public Long getLastReadSequence(Long roomId, Long userId) {
        String key = redisConfig.getLastReadKey(roomId, userId);
        Object sequence = redisTemplate.opsForValue().get(key);
        return sequence != null ? Long.parseLong(sequence.toString()) : 0L;
    }

    public Long getFirstLoadedSequence(Long roomId, Long userId) {
        String key = redisConfig.getFirstLoadedKey(roomId, userId);
        Object sequence = redisTemplate.opsForValue().get(key);
        return sequence != null ? Long.parseLong(sequence.toString()) : 0L;
    }

    public void updateFirstLoadedSequence(Long roomId, Long userId, Long sequence) {
        String key = redisConfig.getFirstLoadedKey(roomId, userId);
        redisTemplate.opsForValue().set(key, sequence);
    }

    public List<String> getAllRoomKeys() {
        Set<String> keys = redisTemplate.keys(redisConfig.getAllSaveMessageKey());
        if (keys == null) {
            return List.of();
        }
        return List.copyOf(keys);
    }

    public List<Object> getAllMessagesFromRoom(String roomKey) {
        return Objects.requireNonNull(redisTemplate.opsForZSet()
                        .rangeByScore(roomKey, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY))
                .stream()
                .toList();
    }

    public Long extractRoomIdFromKey(String roomKey) {
        String prefix = RedisKey.SAVE_MESSAGE_ROOM_ID_KEY.getKey();
        return Long.parseLong(roomKey.replace(prefix, ""));
    }

    public void deleteRoomData(Long roomId) {
        String key = redisConfig.getSaveMessageKey(roomId);
        redisTemplate.delete(key);
    }

    public void updateCurrentSequence(Long roomId, Long sequence) {
        String sequenceKey = redisConfig.getSequenceKey(roomId);
        redisTemplate.opsForValue().set(sequenceKey, sequence);
    }

    public void updateLastReadSequence(ChatMessageDto message) {
        String lastReadKey = redisConfig.getLastReadKey(message.getRoomId(), message.getSenderId());

        redisTemplate.opsForValue().set(lastReadKey, message.getSequence());
    }

    public void updateLastReadSequence(Long roomId, Long userId, Long sequence) {
        String lastReadKey = redisConfig.getLastReadKey(roomId, userId);

        redisTemplate.opsForValue().set(lastReadKey, sequence);
    }

    public Object getLastMessage(Long roomId) {
        String key = redisConfig.getSaveMessageKey(roomId);
        return redisTemplate.opsForList().index(key, -1);
    }
}
