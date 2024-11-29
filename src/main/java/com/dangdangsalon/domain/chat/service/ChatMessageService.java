package com.dangdangsalon.domain.chat.service;

import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import com.dangdangsalon.domain.chat.util.ChatRedisUtil;
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

    private final ObjectMapper objectMapper;
    private final ChatRedisUtil chatRedisUtil;

    public void saveMessageRedis(ChatMessageDto message) {
        chatRedisUtil.saveMessage(message);
    }

    public void updateLastReadKey(ChatMessageDto message) {
        chatRedisUtil.updateLastRead(message);
    }

    public String getLastMessage(Long roomId) {
        Object lastMessage = chatRedisUtil.getLastMessage(roomId);

        if (lastMessage instanceof ChatMessageDto) {
            return ((ChatMessageDto) lastMessage).getMessageText();
        }

        if (lastMessage instanceof LinkedHashMap) {
            return objectMapper.convertValue(lastMessage, ChatMessageDto.class).getMessageText();
        }

        throw new IllegalStateException("데이터가 올바르지 않습니다.");
    }

    public int getUnreadCount(Long roomId, Long userId) {
        Long totalMessageCount = chatRedisUtil.getTotalMessageCount(roomId);
        Integer lastReadIndex = chatRedisUtil.getLastReadIndex(roomId, userId);

        if (totalMessageCount == null) {
            return 0;
        }

        if (lastReadIndex == null) {
            return totalMessageCount.intValue();
        }

        return Math.max(totalMessageCount.intValue() - (lastReadIndex + 1), 0);
    }

    public List<ChatMessageDto> getUnreadOrRecentMessages(Long roomId, Long userId) {
        Integer lastReadIndex = chatRedisUtil.getLastReadIndex(roomId, userId);
        Long totalMessageCount = chatRedisUtil.getTotalMessageCount(roomId);

        List<Object> rawMessages = chatRedisUtil.getMessagesForUnreadOrRecent(roomId, lastReadIndex, totalMessageCount);

        chatRedisUtil.updateLastReadMessage(roomId, userId);

        return rawMessages.stream()
                .map(rawMessage -> objectMapper.convertValue(rawMessage, ChatMessageDto.class))
                .toList();
    }

    public List<ChatMessageDto> getPreviousMessages(Long roomId, Long userId) {
        List<Object> rawMessages = chatRedisUtil.getPreviousMessages(roomId, userId);

        return rawMessages.stream()
                .map(rawMessage -> objectMapper.convertValue(rawMessage, ChatMessageDto.class))
                .toList();
    }

    public void deleteRedisData(Long roomId) {
        chatRedisUtil.deleteRoomData(roomId);
    }
}
