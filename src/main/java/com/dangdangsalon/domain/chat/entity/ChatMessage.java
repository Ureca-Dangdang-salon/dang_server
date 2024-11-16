package com.dangdangsalon.domain.chat.entity;

import com.dangdangsalon.config.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", name = "message_text")
    private String messageText;

    @Column(name = "image_key")
    private String imageKey;

    @Column(name = "send_at")
    private LocalDateTime sendAt;

    @Column(name = "sender_id")
    private Long senderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_role")
    private SenderRole senderRole;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom chatRoom;

    @Builder
    public ChatMessage(String messageText, String imageKey, LocalDateTime sendAt, Long senderId,
                       SenderRole senderRole, ChatRoom chatRoom) {
        this.messageText = messageText;
        this.imageKey = imageKey;
        this.sendAt = sendAt;
        this.senderId = senderId;
        this.senderRole = senderRole;
        this.chatRoom = chatRoom;
    }
}
