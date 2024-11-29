package com.dangdangsalon.domain.chat.entity;

import com.dangdangsalon.domain.chat.dto.ChatMessageDto;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Builder.ObtainVia;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "chat_messages")
public class ChatMessageMongo {

    @Id
    private String id;

    private Long roomId;
    private Long senderId;
    private String senderRole;
    private String messageText;
    private String imageUrl;
    private LocalDateTime sendAt;

    @Builder
    public ChatMessageMongo(String id, Long roomId, Long senderId, String senderRole, String messageText,
                            String imageUrl,
                            LocalDateTime sendAt) {
        this.id = id;
        this.roomId = roomId;
        this.senderId = senderId;
        this.senderRole = senderRole;
        this.messageText = messageText;
        this.imageUrl = imageUrl;
        this.sendAt = sendAt;
    }

    public static ChatMessageMongo createMessage(ChatMessageDto message) {
        return ChatMessageMongo.builder()
                .id(message.getMessageId())
                .roomId(message.getRoomId())
                .senderId(message.getSenderId())
                .senderRole(message.getSenderRole())
                .messageText(message.getMessageText())
                .imageUrl(message.getImageUrl())
                .sendAt(message.getSendAt())
                .build();
    }
}
