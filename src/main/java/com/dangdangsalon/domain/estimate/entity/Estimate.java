package com.dangdangsalon.domain.estimate.entity;

import com.dangdangsalon.config.base.BaseEntity;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "estimate")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Estimate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int aggressionCharge;

    private int healthIssueCharge;

    @Enumerated(EnumType.STRING)
    private EstimateStatus status;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String imageKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groomer_profile_id")
    private GroomerProfile groomerProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private EstimateRequest estimateRequest;

    @Builder
    public Estimate(int aggressionCharge, int healthIssueCharge, EstimateStatus status, String description,
                    String imageKey,
                    GroomerProfile groomerProfile, EstimateRequest estimateRequest) {
        this.aggressionCharge = aggressionCharge;
        this.healthIssueCharge = healthIssueCharge;
        this.status = status;
        this.description = description;
        this.imageKey = imageKey;
        this.groomerProfile = groomerProfile;
        this.estimateRequest = estimateRequest;
    }
}
