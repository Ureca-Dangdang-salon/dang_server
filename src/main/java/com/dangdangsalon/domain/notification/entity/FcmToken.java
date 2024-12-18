package com.dangdangsalon.domain.notification.entity;

import com.dangdangsalon.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "fcm_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fcm_token_id")
    private Long id;

    private String fcmToken;

    private LocalDateTime lastUserAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public FcmToken(String fcmToken, LocalDateTime lastUserAt, User user) {
        this.fcmToken = fcmToken;
        this.lastUserAt = lastUserAt;
        this.user = user;
    }

    public void updateTokenLastUserAt() {
        this.lastUserAt = LocalDateTime.now();
    }

}
