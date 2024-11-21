package com.dangdangsalon.domain.estimate.request.entity;
import com.dangdangsalon.config.base.BaseEntity;
import com.dangdangsalon.domain.dogprofile.entity.DogProfile;
import jakarta.persistence.*;
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

    @Column(name = "current_photo_key")
    private String currentImageKey;

    @Column(name = "style_ref_photo_key")
    private String styleRefImageKey;

    private boolean aggression;

    @Column(name = "health_issue")
    private boolean healthIssue;

    @Column(columnDefinition = "TEXT")
    private String description;

    private int aggressionCharge;

    private int healthIssueCharge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private EstimateRequest estimateRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private DogProfile dogProfile;

    @Builder
    public EstimateRequestProfiles(EstimateRequest estimateRequest, DogProfile dogProfile, String currentImageKey, String styleRefImageKey, boolean aggression, boolean healthIssue, String description, int aggressionCharge, int healthIssueCharge) {
        this.estimateRequest = estimateRequest;
        this.dogProfile = dogProfile;
        this.currentImageKey = currentImageKey;
        this.styleRefImageKey = styleRefImageKey;
        this.aggression = aggression;
        this.healthIssue = healthIssue;
        this.description = description;
        this.aggressionCharge = aggressionCharge;
        this.healthIssueCharge = healthIssueCharge;
    }

    public void updateCharges(int aggressionCharge, int healthIssueCharge) {
        this.aggressionCharge = aggressionCharge;
        this.healthIssueCharge = healthIssueCharge;
    }
}