package com.dangdangsalon.domain.estimate.request.entity;

import com.dangdangsalon.config.base.BaseEntity;
import com.dangdangsalon.domain.dogprofile.entity.DogProfile;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "estimate_request_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EstimateRequestProfiles extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private EstimateRequest estimateRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private DogProfile dogProfile;

    @Builder
    public EstimateRequestProfiles(EstimateRequest estimateRequest, DogProfile dogProfile) {
        this.estimateRequest = estimateRequest;
        this.dogProfile = dogProfile;
    }
}
