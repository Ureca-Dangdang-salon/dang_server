package com.dangdangsalon.domain.chat.service;

import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import com.dangdangsalon.domain.chat.entity.ChatMessageMongo;
import com.dangdangsalon.domain.chat.repository.ChatMessageRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatMessageMongoService {

    private final ChatMessageRepository chatMessageRepository;

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
        Pageable pageable = PageRequest.of(0, limit, Sort.by("sequence").descending());
        List<ChatMessageDto> messages = new ArrayList<>(
                chatMessageRepository.findByRoomIdAndSequenceLessThanOrderBySequenceDesc(roomId,
                                firstLoadedSequence, pageable)
                        .stream()
                        .map(ChatMessageDto::mongoMessageToDto)
                        .toList()); //스트림의 toList()는 기본적으로 불변리스트 생성함. 따라서 아래 addAll() 메서드 실패.

        if (messages.size() < limit) {
            Long lastSequence = messages.isEmpty() ? firstLoadedSequence : messages.get(messages.size() - 1).getSequence();
            List<ChatMessageDto> additionalMessages = chatMessageRepository.findByRoomIdAndSequenceLessThanOrderBySequenceDesc(
                            roomId, lastSequence, PageRequest.of(0, limit - messages.size() - 1, Sort.by("sequence").descending()))
                    .stream()
                    .map(ChatMessageDto::mongoMessageToDto)
                    .toList();

            messages.addAll(additionalMessages);
        }

        return messages;
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
        List<ChatMessageMongo> recentMessages = chatMessageRepository.findTopByRoomIdOrderBySequenceDesc(roomId, pageable);

        if (recentMessages.isEmpty()) {
            return 0L;
        }

        return recentMessages.get(0).getSequence();
    }
}
