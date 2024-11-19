package com.dangdangsalon.domain.groomerprofile.request.entity;

import com.dangdangsalon.config.base.BaseEntity;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroomerEstimateRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private GroomerRequestStatus groomerRequestStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private EstimateRequest estimateRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groomer_profile_id")
    private GroomerProfile groomerProfile;

    @Builder
    public GroomerEstimateRequest(GroomerRequestStatus groomerRequestStatus, EstimateRequest estimateRequest, GroomerProfile groomerProfile) {
        this.groomerRequestStatus = groomerRequestStatus;
        this.estimateRequest = estimateRequest;
        this.groomerProfile = groomerProfile;
    }
}
