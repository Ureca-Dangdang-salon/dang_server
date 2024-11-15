package com.dangdangsalon.domain.chat.entity;

import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long id;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "customer_left")
    private boolean customerLeft;

    @Column(name = "groomer_left")
    private boolean groomerLeft;

    @ManyToOne
    @JoinColumn(name = "estimate_id", nullable = false)
    private Estimate estimate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private GroomerProfile groomerProfile;

    @Builder
    public ChatRoom(LocalDateTime createdAt, Boolean customerLeft, Boolean groomerLeft,
                    Estimate estimate, User user, GroomerProfile groomerProfile) {
        this.createdAt = createdAt;
        this.customerLeft = customerLeft;
        this.groomerLeft = groomerLeft;
        this.estimate = estimate;
        this.user = user;
        this.groomerProfile = groomerProfile;
    }
}
