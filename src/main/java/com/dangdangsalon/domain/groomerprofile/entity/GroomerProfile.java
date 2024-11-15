package com.dangdangsalon.domain.groomerprofile.entity;

import com.dangdangsalon.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "groomer_profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroomerProfile {

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
}

