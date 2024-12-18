package com.dangdangsalon.domain.groomerprofile.entity;

import com.dangdangsalon.config.base.BaseEntity;
import com.dangdangsalon.domain.dogprofile.entity.DogAge;
import com.dangdangsalon.domain.dogprofile.entity.DogProfile;
import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.groomerprofile.repository.GroomerServiceAreaRepository;
import com.dangdangsalon.domain.groomerprofile.request.entity.GroomerEstimateRequest;
import com.dangdangsalon.domain.groomerprofile.review.entity.Review;
import com.dangdangsalon.domain.mypage.dto.req.DogProfileRequestDto;
import com.dangdangsalon.domain.mypage.dto.req.GroomerProfileRequestDto;
import com.dangdangsalon.domain.orders.entity.Orders;
import com.dangdangsalon.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "groomer_profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroomerProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String contactHours;

    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    private String phone;

    @Column(name = "image_key")
    private String imageKey;

    @Embedded
    private GroomerDetails details;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;


    @OneToMany(mappedBy = "groomerProfile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    private List<GroomerCertification> groomerCertifications = new ArrayList<>();

    @OneToMany(mappedBy = "groomerProfile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "groomerProfile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    private List<GroomerEstimateRequest> groomerEstimateRequests = new ArrayList<>();

    @OneToMany(mappedBy = "groomerProfile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    private List<GroomerCanService> groomerCanServices = new ArrayList<>();

    @OneToMany(mappedBy = "groomerProfile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    private List<GroomerServiceArea> groomerServiceAreas = new ArrayList<>();

    @OneToMany(mappedBy = "groomerProfile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    private List<Estimate> estimates = new ArrayList<>();

    @Builder
    public GroomerProfile(String name, String contactHours, ServiceType serviceType, String phone, String imageKey,
                          GroomerDetails details, User user) {
        this.name = name;
        this.contactHours = contactHours;
        this.serviceType = serviceType;
        this.phone = phone;
        this.imageKey = imageKey;
        this.details = details;
        this.user = user;
    }

    public void updateProfileDetail(String imageKey, GroomerDetails details) {
        this.imageKey = imageKey;
        this.details = details;
    }

    public void updateProfile(String name, String contactHours, ServiceType serviceType, String phone, String imageKey,
                              GroomerDetails details) {
        this.name = name;
        this.contactHours = contactHours;
        this.serviceType = serviceType;
        this.phone = phone;
        this.imageKey = imageKey;
        this.details = details;
    }

    public boolean isValidUser(Long userId) {
        return this.getUser().getId().equals(userId);
    }

    public static GroomerProfile createGroomerProfile(GroomerProfileRequestDto requestDto, User user) {
        return GroomerProfile.builder()
                .name(requestDto.getName())
                .phone(requestDto.getPhone())
                .contactHours(requestDto.getContactHours())
                .serviceType(requestDto.getServiceType())
                .details(null)
                .user(user)
                .build();
    }
}

