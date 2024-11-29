package com.dangdangsalon.domain.chat.entity;

import com.dangdangsalon.config.base.BaseEntity;
import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.user.entity.Role;
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
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_left", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean customerLeft;

    @Column(name = "groomer_left", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean groomerLeft;

    @ManyToOne
    @JoinColumn(name = "estimate_id")
    private Estimate estimate;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private User customer;

    @ManyToOne
    @JoinColumn(name = "groomer_id")
    private User groomer;

    @Builder
    public ChatRoom(Boolean customerLeft, Boolean groomerLeft, Estimate estimate, User customer, User groomer) {
        this.customerLeft = customerLeft;
        this.groomerLeft = groomerLeft;
        this.estimate = estimate;
        this.customer = customer;
        this.groomer = groomer;
    }

    public void updateExitState(Role userRole, boolean state) {
        if (userRole.equals(Role.ROLE_USER)) {
            this.customerLeft = state;
        } else if (userRole.equals(Role.ROLE_SALON)) {
            this.groomerLeft = state;
        }
    }
}