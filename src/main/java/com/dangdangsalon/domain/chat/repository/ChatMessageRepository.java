package com.dangdangsalon.domain.chat.repository;

import com.dangdangsalon.domain.chat.entity.ChatMessage;
import com.dangdangsalon.domain.chat.entity.ChatMessageMongo;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatMessageRepository extends MongoRepository<ChatMessageMongo, String> {
    List<ChatMessageMongo> findByRoomIdOrderBySendAtDesc(Long roomId, Pageable pageable);

    List<ChatMessageMongo> findByRoomIdAndMessageIdGreaterThanOrderBySendAtAsc(Long roomId, Long messageId);

    List<ChatMessageMongo> findByRoomIdAndSendAtLessThan(Long roomId, LocalDateTime sendAt, Pageable pageable);

    void deleteByRoomId(Long roomId);
}
