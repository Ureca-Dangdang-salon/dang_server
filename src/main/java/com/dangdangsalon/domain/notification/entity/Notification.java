package com.dangdangsalon.domain.notification.entity;

import com.dangdangsalon.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private LocalDateTime noticeAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Notification(String name, String description, NotificationStatus status, LocalDateTime noticeAt, User user) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.noticeAt = noticeAt;
        this.user = user;
    }
}
