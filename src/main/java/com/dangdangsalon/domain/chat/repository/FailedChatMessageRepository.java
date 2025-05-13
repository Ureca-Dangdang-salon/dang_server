package com.dangdangsalon.domain.chat.repository;

import com.dangdangsalon.domain.chat.entity.FailedChatMessage;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FailedChatMessageRepository extends MongoRepository<FailedChatMessage, String> {
    List<FailedChatMessage> findByRoomIdAndReplayedFalse(String roomId);
}
