package com.dangdangsalon.domain.chat.service;

import com.dangdangsalon.domain.chat.dto.ChatEstimateInfo;
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
        Long sequence = chatRedisUtil.getNextSequence(message.getRoomId());
        message.updateSequence(sequence);

        chatRedisUtil.saveMessage(message);
    }

    public void updateLastReadKey(ChatMessageDto message) {
        chatRedisUtil.updateLastReadSequence(message);
    }

    public String getLastMessage(Long roomId) {
        Object lastMessage = chatRedisUtil.getLastMessage(roomId);

        if (lastMessage instanceof LinkedHashMap) {
            ChatMessageDto chatMessage = objectMapper.convertValue(lastMessage, ChatMessageDto.class);
            String messageText = chatMessage.getMessageText();
            String imageUrl = chatMessage.getImageUrl();
            ChatEstimateInfo estimateInfo = chatMessage.getEstimateInfo();

            if (messageText != null) {
                return messageText;
            } else if (imageUrl != null) {
                return "사진을 보냈습니다.";
            } else if (estimateInfo != null) {
                return "견적서를 보냈습니다.";
            }
        }

        if (lastMessage == null) {
            log.info("Redis에 메시지 X -> MongoDB 조회");
            List<ChatMessageDto> mongoMessages = chatMessageMongoService.getChatMessagesInMongo(roomId, 0, 1);
            ChatMessageDto chatMessage = mongoMessages.get(0);
            String messageText = chatMessage.getMessageText();
            String imageUrl = chatMessage.getImageUrl();
            ChatEstimateInfo estimateInfo = chatMessage.getEstimateInfo();

            if (messageText != null) {
                return messageText;
            } else if (imageUrl != null) {
                return "사진을 보냈습니다.";
            } else if (estimateInfo != null) {
                return "견적서를 보냈습니다.";
            }
        }

        return "최신 메시지가 없습니다.";
    }

    public int getUnreadCount(Long roomId, Long userId) {
        Long totalSequence = chatRedisUtil.getCurrentSequence(roomId);
        if (totalSequence == null) {
            log.info("Redis 시퀀스 조회 불가 -> MongoDB에서 시퀀스 조회");

            totalSequence = chatMessageMongoService.getMaxSequence(roomId);

            if (totalSequence == null) {
                return 0;
            }
        }

        Long lastReadSequence = chatRedisUtil.getLastReadSequence(roomId, userId);

        if (lastReadSequence == null) {
            return totalSequence.intValue();
        }

        return totalSequence.intValue() - lastReadSequence.intValue();
    }

    public List<ChatMessageDto> getUnreadOrRecentMessages(Long roomId, Long userId) {
        Long lastReadSequence = chatRedisUtil.getLastReadSequence(roomId, userId);
        log.info("lastReadSequence = " + lastReadSequence);
        Long currentSequence = chatRedisUtil.getCurrentSequence(roomId);
        log.info("currentSequence = " + currentSequence);
        Long newFirstLoadedSequence = 0L;

        //읽지 않은 메시지가 있는 경우 (읽지 않은 메시지 ~ 최신 메시지)
        if (lastReadSequence != null && lastReadSequence < currentSequence) {
            List<ChatMessageDto> unreadMessages = getFromUnreadMessagesToCurrentMessage(roomId,
                    userId, lastReadSequence, currentSequence);

            newFirstLoadedSequence = unreadMessages.get(0).getSequence();
            chatRedisUtil.updateFirstLoadedSequence(roomId, userId, newFirstLoadedSequence);

            return unreadMessages;
        }

        //읽지 않은 메시지가 없는 경우 -> 최신 메시지부터 N개 조회
        List<ChatMessageDto> recentMessages = getRecentMessages(roomId, currentSequence);

        newFirstLoadedSequence = recentMessages.get(0).getSequence();
        chatRedisUtil.updateFirstLoadedSequence(roomId, userId, newFirstLoadedSequence);
        return recentMessages;
    }

    public List<ChatMessageDto> getPreviousMessages(Long roomId, Long userId) {
        Long firstLoadedSequence = chatRedisUtil.getFirstLoadedSequence(roomId, userId);

        if (firstLoadedSequence == null || firstLoadedSequence <= 0) {
            return List.of();
        }

        List<Object> redisMessages = chatRedisUtil.getMessagesBySequence(
                roomId, Math.max(0L, firstLoadedSequence - ChatConst.MESSAGE_GET_LIMIT.getCount()),
                firstLoadedSequence - 1
        );

        if (!redisMessages.isEmpty()) {
            ChatMessageDto firstMessage = objectMapper.convertValue(redisMessages.get(0), ChatMessageDto.class);
            Long newFirstLoadedSequence = firstMessage.getSequence();
            chatRedisUtil.updateFirstLoadedSequence(roomId, userId, newFirstLoadedSequence);

            return redisMessages.stream()
                    .map(message -> objectMapper.convertValue(message, ChatMessageDto.class))
                    .toList();
        }

        log.info("Redis에 이전 메시지 X -> MongoDB 조회");
        List<ChatMessageDto> mongoMessages = chatMessageMongoService.getMessagesBeforeSequence(
                roomId, firstLoadedSequence, ChatConst.MESSAGE_GET_LIMIT.getCount()
        );

        if (!mongoMessages.isEmpty()) {
            Long newFirstLoadedSequence = mongoMessages.get(0).getSequence();
            log.info("newFirstLoadedSequence= " + newFirstLoadedSequence);
            chatRedisUtil.updateFirstLoadedSequence(roomId, userId, newFirstLoadedSequence);
        }

        return mongoMessages;
    }

    public void deleteChatData(Long roomId) {
        chatRedisUtil.deleteRoomData(roomId);
        chatMessageMongoService.deleteChatMessages(roomId);
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

    private List<ChatMessageDto> getFromUnreadMessagesToCurrentMessage(Long roomId, Long userId, Long lastReadSequence,
                                                                       Long currentSequence) {
        List<Object> redisMessages = chatRedisUtil.getMessagesBySequence(roomId, lastReadSequence + 1, currentSequence);

        if (!redisMessages.isEmpty()) {
            chatRedisUtil.updateLastReadSequence(roomId, userId, currentSequence);
            return redisMessages.stream()
                    .map(message -> objectMapper.convertValue(message, ChatMessageDto.class))
                    .toList();
        }

        log.info("Redis 데이터 X -> MongoDB 조회 roomId: {}, lastReadSequence: {}", roomId, lastReadSequence);
        List<ChatMessageDto> mongoMessages = chatMessageMongoService.getMessagesFromSequence(roomId,
                lastReadSequence, ChatConst.MESSAGE_GET_LIMIT.getCount());

        Long lastSequence = mongoMessages.get(mongoMessages.size() - 1).getSequence();
        chatRedisUtil.updateLastReadSequence(roomId, userId, lastSequence);

        return mongoMessages;
    }

    private List<ChatMessageDto> getRecentMessages(Long roomId, Long currentSequence) {
        List<Object> recentRedisMessages = chatRedisUtil.getMessagesBySequence(
                roomId, currentSequence - ChatConst.MESSAGE_GET_LIMIT.getCount() + 1, currentSequence);

        if (!recentRedisMessages.isEmpty()) {
            return recentRedisMessages.stream()
                    .map(message -> objectMapper.convertValue(message, ChatMessageDto.class))
                    .toList();
        }

        log.info("Redis 데이터 X -> MongoDB 조회 roomId: {}, currentSequence: {}", roomId, currentSequence);
        return chatMessageMongoService.getMessagesFromSequence(roomId,
                currentSequence - ChatConst.MESSAGE_GET_LIMIT.getCount(), ChatConst.MESSAGE_GET_LIMIT.getCount());
    }
}
