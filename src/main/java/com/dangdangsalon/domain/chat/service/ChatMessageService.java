package com.dangdangsalon.domain.chat.service;

import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import com.dangdangsalon.domain.chat.util.ChatConst;
import com.dangdangsalon.domain.chat.util.ChatRedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDateTime;
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
    private final ChatMessageMongoService chatMessageMongoService;

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

        if (lastMessage == null) {
            log.info("Redis에 메시지 X -> MongoDB 조회");
            List<ChatMessageDto> mongoMessages = chatMessageMongoService.getChatMessagesInMongo(roomId, 0, 1);
            return mongoMessages.get(0).getMessageText();
        }

        return "최신 메시지가 없습니다.";
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

        List<Object> redisMessages = chatRedisUtil.getMessagesForUnreadOrRecent(roomId, lastReadIndex,
                totalMessageCount);

        if (!redisMessages.isEmpty()) {
            chatRedisUtil.updateLastReadMessage(roomId, userId);
            return redisMessages.stream()
                    .map(messages -> objectMapper.convertValue(messages, ChatMessageDto.class))
                    .toList();
        }

        log.info("Redis 메시지 데이터 X -> MongoDB 조회");
        List<ChatMessageDto> mongoMessages;
        String lastReadMessageId = null;

        if (lastReadIndex != null) {
            Object lastReadMessage = chatRedisUtil.getLastReadMessage(roomId, lastReadIndex);

            if (lastReadMessage != null) {
                ChatMessageDto messageDto = objectMapper.convertValue(lastReadMessage, ChatMessageDto.class);
                lastReadMessageId = messageDto.getMessageId();
            }
        }

        if (lastReadMessageId == null) {
            mongoMessages = chatMessageMongoService.getChatMessagesInMongo(roomId, 0, ChatConst.MESSAGE_GET_LIMIT.getCount());
        } else {
            mongoMessages = chatMessageMongoService.getUnreadMessages(roomId, lastReadMessageId);
        }

        if (!mongoMessages.isEmpty()) {
            chatRedisUtil.updateLastReadMessage(roomId, userId);
        }

        return mongoMessages;
    }

    public List<ChatMessageDto> getPreviousMessages(Long roomId, Long userId) {
        List<Object> redisMessages = chatRedisUtil.getPreviousMessages(roomId, userId);

        if (!redisMessages.isEmpty()) {
            return redisMessages.stream()
                    .map(rawMessage -> objectMapper.convertValue(rawMessage, ChatMessageDto.class))
                    .toList();
        }

        log.info("Redis 메시지 데이터 X -> MongoDB 조회");
        LocalDateTime beforeMessageSendAt = getLastMessageSendAtFromRedis(roomId, userId);
        return chatMessageMongoService.getPreviousMessages(roomId, beforeMessageSendAt,
                ChatConst.MESSAGE_GET_LIMIT.getCount());
    }

    public void deleteChatData(Long roomId) {
        chatRedisUtil.deleteRoomData(roomId);
        chatMessageMongoService.deleteChatMessages(roomId);
    }

    private LocalDateTime getLastMessageSendAtFromRedis(Long roomId, Long userId) {
        Integer lastLoadedIndex = chatRedisUtil.getFirstLoadedIndex(roomId, userId);

        if (lastLoadedIndex != null) {
            ChatMessageDto lastLoadedMessage = objectMapper.convertValue(
                    chatRedisUtil.getMessageAtIndex(roomId, lastLoadedIndex), ChatMessageDto.class);

            if (lastLoadedMessage != null) {
                return lastLoadedMessage.getSendAt();
            }
        }

        return LocalDateTime.now();
    }

    public void saveAllMessagesMongo() {
        List<String> roomKeys = chatRedisUtil.getAllRoomKeys();

        for (String roomKey : roomKeys) {
            Long roomId = chatRedisUtil.extractRoomIdFromKey(roomKey);

            List<Object> redisMessages = chatRedisUtil.getAllMessagesFromRoom(roomKey);

            List<ChatMessageDto> chatMessages = redisMessages.stream()
                    .map(message -> objectMapper.convertValue(message, ChatMessageDto.class))
                    .toList();

            chatMessages.forEach(chatMessageMongoService::saveChatMessage);

            chatRedisUtil.deleteRoomData(roomId);
        }
    }
}
