package com.dangdangsalon.domain.estimate.entity;
import com.dangdangsalon.config.base.BaseEntity;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
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
public class Estimate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private EstimateStatus status;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String imageKey;

    private int totalAmount;

    private LocalDateTime date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groomer_profile_id")
    private GroomerProfile groomerProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private EstimateRequest estimateRequest;

    @Builder
    public Estimate(EstimateStatus status, String description,
                    String imageKey,
                    GroomerProfile groomerProfile, EstimateRequest estimateRequest, int totalAmount, LocalDateTime date) {
        this.status = status;
        this.description = description;
        this.imageKey = imageKey;
        this.groomerProfile = groomerProfile;
        this.estimateRequest = estimateRequest;
        this.totalAmount = totalAmount;
        this.date = date;
    }

    public void updateStatus(EstimateStatus status) {
        this.status = status;
    }
}
