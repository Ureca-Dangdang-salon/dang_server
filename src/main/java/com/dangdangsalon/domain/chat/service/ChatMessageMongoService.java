package com.dangdangsalon.domain.chat.service;

import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import com.dangdangsalon.domain.chat.entity.ChatMessageMongo;
import com.dangdangsalon.domain.chat.repository.ChatMessageRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageMongoService {

    private final ChatMessageRepository chatMessageRepository;
    private final MongoTemplate mongoTemplate;

    public void saveChatMessage(ChatMessageDto message) {
        ChatMessageMongo savedChatMessage = ChatMessageMongo.createMessage(message);
        chatMessageRepository.save(savedChatMessage);
    }

    public List<ChatMessageDto> getMessagesFromSequence(Long roomId, Long lastReadSequence, int limit) {
        return chatMessageRepository.findByRoomIdAndSequenceGreaterThanOrderBySequenceAsc(roomId, lastReadSequence)
                .stream()
                .map(ChatMessageDto::mongoMessageToDto)
                .limit(limit)
                .toList();
    }

    public List<ChatMessageDto> getMessagesBeforeSequence(Long roomId, Long firstLoadedSequence, int limit) {
//        List<ChatMessageDto> messages = chatMessageRepository.findByRoomIdAndSequenceLessThanOrderBySequenceAsc(
//                        roomId, firstLoadedSequence, limit)
//                .stream()
//                .map(ChatMessageDto::mongoMessageToDto)
//                .toList();

        int startSequence = calculateStartSequence(firstLoadedSequence, limit);

        Query query = new Query()
                .addCriteria(
                        Criteria.where("roomId").is(roomId).and("sequence").gte(startSequence).lt(firstLoadedSequence))
                .with(Sort.by(Sort.Direction.ASC, "sequence"))
                .limit(limit);

        List<ChatMessageMongo> messages = mongoTemplate.find(query, ChatMessageMongo.class);

        if (messages.isEmpty()) {
            return List.of();
        }

        return messages.stream()
                .map(ChatMessageDto::mongoMessageToDto)
                .toList();

        // 결과 반환
//        return messages;

//        if (messages.size() < limit) {
//            Long lastSequence = messages.get(messages.size() - 1).getSequence();
//            List<ChatMessageDto> additionalMessages = chatMessageRepository.findByRoomIdAndSequenceLessThanOrderBySequenceAsc(
//                            roomId, lastSequence, pageable)
//                    .stream()
//                    .map(ChatMessageDto::mongoMessageToDto)
//                    .toList();
//
//            messages.addAll(additionalMessages);
//        }
    }

    public void deleteChatMessages(Long roomId) {
        chatMessageRepository.deleteByRoomId(roomId);
    }

    public List<ChatMessageDto> getChatMessagesInMongo(Long roomId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sendAt").descending());
        return chatMessageRepository.findByRoomIdOrderBySendAtDesc(roomId, pageable)
                .stream()
                .map(ChatMessageDto::mongoMessageToDto)
                .toList();
    }

    public Long getMaxSequence(Long roomId) {
        Pageable pageable = PageRequest.of(0, 1, Sort.by("sequence").descending());
        List<ChatMessageMongo> recentMessages = chatMessageRepository.findTopByRoomIdOrderBySequenceDesc(roomId,
                pageable);

        if (recentMessages.isEmpty()) {
            return 0L;
        }

        return recentMessages.get(0).getSequence();
    }

    private int calculateStartSequence(Long firstLoadedSequence, int limit) {
        return Math.max(0, (int) (firstLoadedSequence - limit));
    }
}
