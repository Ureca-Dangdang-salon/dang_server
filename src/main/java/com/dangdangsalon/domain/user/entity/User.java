package com.dangdangsalon.domain.user.entity;

import com.dangdangsalon.config.base.BaseEntity;
import com.dangdangsalon.domain.coupon.entity.Coupon;
import com.dangdangsalon.domain.dogprofile.entity.DogProfile;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.groomerprofile.review.entity.Review;
import com.dangdangsalon.domain.orders.entity.Orders;
import com.dangdangsalon.domain.region.entity.District;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String name;

    private String email;

    @Column(name = "image_key")
    private String imageKey;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    private List<DogProfile> dogProfiles = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    private List<Coupon> coupons = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    private List<Orders> orders = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private District district;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private GroomerProfile groomerProfile;

    @Column(name = "notification_enabled")
    private Boolean notificationEnabled = true;

    @Builder
    public User(Long id, String username, String name, String email, String imageKey, Role role, District district) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.email = email;
        this.imageKey = imageKey;
        this.role = role;
        this.district = district;
        this.notificationEnabled = notificationEnabled;
    }

    public void updateAdditionalInfo(Role role, District district) {
        this.role = role;
        this.district = district;
    }

    public void updateRole(Role role) {
        this.role = role;
    }

    public void updateUserInfo(String imageKey, String email, District district) {
        this.imageKey = imageKey;
        this.email = email;
        this.district = district;
    }

    public void updateNotificationEnabled(Boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }
}
