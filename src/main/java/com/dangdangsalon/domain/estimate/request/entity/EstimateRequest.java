package com.dangdangsalon.domain.estimate.request.entity;

import com.dangdangsalon.domain.groomerprofile.entity.ServiceType;
import com.dangdangsalon.domain.region.entity.District;
import com.dangdangsalon.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "estimate_request")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EstimateRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime requestDate;

    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus;

    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    @Column(name = "current_photo_key")
    private String currentImageKey;

    @Column(name = "style_ref_photo_key")
    private String styleRefImageKey;

    private boolean aggression;

    @Column(name = "health_issue")
    private boolean healthIssue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private District district;

    public EstimateRequest(LocalDateTime requestDate, RequestStatus requestStatus, ServiceType serviceType,
                           String currentImageKey, String styleRefImageKey, boolean aggression, boolean healthIssue,
                           User user, District district) {
        this.requestDate = requestDate;
        this.requestStatus = requestStatus;
        this.serviceType = serviceType;
        this.currentImageKey = currentImageKey;
        this.styleRefImageKey = styleRefImageKey;
        this.aggression = aggression;
        this.healthIssue = healthIssue;
        this.user = user;
        this.district = district;
    }
}
