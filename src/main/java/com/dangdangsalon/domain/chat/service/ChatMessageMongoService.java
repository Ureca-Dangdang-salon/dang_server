package com.dangdangsalon.domain.chat.service;

import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import com.dangdangsalon.domain.chat.entity.ChatMessageMongo;
import com.dangdangsalon.domain.chat.repository.ChatMessageRepository;
import java.time.LocalDateTime;
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

    public List<ChatMessageDto> getChatMessagesInMongo(Long roomId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sendAt").descending());

        return chatMessageRepository.findByRoomIdOrderBySendAtDesc(roomId, pageable)
                .stream()
                .map(ChatMessageDto::mongoMessageToDto)
                .toList();
    }

    public List<ChatMessageDto> getUnreadMessages(Long roomId, Integer lastReadMessageId) {
        return chatMessageRepository.findByRoomIdAndMessageIdGreaterThanOrderBySendAtAsc(roomId, lastReadMessageId)
                .stream()
                .map(ChatMessageDto::mongoMessageToDto)
                .toList();
    }

    public List<ChatMessageDto> getPreviousMessages(Long roomId, LocalDateTime beforeMessageSendAt, int size) {
        Pageable pageable = PageRequest.of(0, size, Sort.by("sendAt").descending());
        return chatMessageRepository.findByRoomIdAndSendAtLessThan(roomId, beforeMessageSendAt, pageable)
                .stream()
                .map(ChatMessageDto::mongoMessageToDto)
                .toList();
    }

    public void deleteChatMessages(Long roomId) {
        chatMessageRepository.deleteByRoomId(roomId);
    }
}
