package com.dangdangsalon.domain.notification.entity;

import com.dangdangsalon.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topic_id")
    private Long id;

    private String topicName;

    private Boolean subscribe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Topic(String topicName, Boolean subscribe, User user) {
        this.topicName = topicName;
        this.subscribe = subscribe;
        this.user = user;
    }

    public void updateSubscribe(Boolean subscribe) {
        this.subscribe = subscribe;
    }
}
