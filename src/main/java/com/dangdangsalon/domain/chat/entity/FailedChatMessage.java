package com.dangdangsalon.domain.chat.entity;

import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "chat_message_dlq")
public class FailedChatMessage {

    @Id
    private String id;

    private Long roomId;
    private String message;
    private String exception;
    private LocalDateTime failedAt;
    private boolean replayed;

    private String topic;
    private int partition;

    @Builder
    public FailedChatMessage(String id, Long roomId, String message, String exception, LocalDateTime failedAt,
                             boolean replayed, String topic, int partition) {
        this.id = id;
        this.roomId = roomId;
        this.message = message;
        this.exception = exception;
        this.failedAt = failedAt;
        this.replayed = replayed;
        this.topic = topic;
        this.partition = partition;
    }

    public void updateReplayed(boolean isReplay) {
        this.replayed = isReplay;
    }
}
