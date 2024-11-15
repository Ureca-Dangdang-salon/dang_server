package com.dangdangsalon.domain.estimate.entity;

import com.dangdangsalon.domain.estimaterequest.entity.EstimateRequest;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

@Entity
@Table(name = "estimate")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Estimate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "estimate_id")
    private Long id;

    @Column(name = "aggression_charge")
    private int aggression_charge;

    @Column(name = "health_issue_charge")
    private int healthIssueCharge;

    private EstimateStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "image_key")
    private String imageKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private GroomerProfile groomerProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private EstimateRequest estimateRequest;

    @Builder
    public Estimate(int aggression_charge, int healthIssueCharge, EstimateStatus status, LocalDateTime createdAt,
                    String imageKey, GroomerProfile groomerProfile, EstimateRequest estimateRequest) {
        this.aggression_charge = aggression_charge;
        this.healthIssueCharge = healthIssueCharge;
        this.status = status;
        this.createdAt = createdAt;
        this.imageKey = imageKey;
        this.groomerProfile = groomerProfile;
        this.estimateRequest = estimateRequest;
    }
}
