package com.dangdangsalon.domain.contest.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "contest")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Contest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contest_id")
    private Long id;

    private String title;

    private String description;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Builder
    public Contest(String title, String description, LocalDateTime startedAt, LocalDateTime endAt) {
        this.title = title;
        this.description = description;
        this.startedAt = startedAt;
        this.endAt = endAt;
    }
}
