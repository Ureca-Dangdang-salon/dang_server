package com.dangdangsalon.domain.estimaterequest.entity;

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
    @Column(name = "request_id")
    private Long id;

    private LocalDateTime date;

    private String region;

    private RequestStatus status;

    @Column(name = "service_type")
    private ServiceType serviceType;

    @Column(name = "current_photo_key")
    private String currentPhotoKey;

    @Column(name = "style_ref_photo_key")
    private String styleRefPhotoKey;

    private boolean aggression;

    @Column(name = "health_issue")
    private boolean healthIssue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private District district;
}
