package com.dangdangsalon.domain.estimate.request.entity;
import com.dangdangsalon.config.base.BaseEntity;
import com.dangdangsalon.domain.groomerprofile.entity.ServiceType;
import com.dangdangsalon.domain.region.entity.District;
import com.dangdangsalon.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "estimate_request")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EstimateRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime requestDate;

    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus;

    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private District district;

    @OneToMany(mappedBy = "estimateRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EstimateRequestProfiles> estimateRequestProfiles = new ArrayList<>();

    @Builder
    public EstimateRequest(LocalDateTime requestDate, RequestStatus requestStatus, ServiceType serviceType,
                           User user, District district, List<EstimateRequestProfiles> estimateRequestProfiles) {
        this.requestDate = requestDate;
        this.requestStatus = requestStatus;
        this.serviceType = serviceType;
        this.user = user;
        this.district = district;
        this.estimateRequestProfiles = estimateRequestProfiles;
    }

    public void updateRequestStatus(RequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }
}