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
    @Column(name = "profile_id")
    private Long id;

    private String name;

    @Column(name = "contact_hours")
    private String contactHours;

    @Column(name = "service_type")
    private ServiceType serviceType;

    @Column(name = "business_number")
    private String businessNumber;

    private String certification;

    private String description;

    @Column(name = "chat_start")
    private String chatStart;

    private String address;

    @Column(columnDefinition = "TEXT")
    private String faq;

    private String phone;

    @Column(name = "image_key")
    private String imageKey;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public GroomerProfile(String name, String contactHours, ServiceType serviceType, String businessNumber,
                          String certification, String description, String chatStart, String address,
                          String faq, String phone, String imageKey, User user) {
        this.name = name;
        this.contactHours = contactHours;
        this.serviceType = serviceType;
        this.businessNumber = businessNumber;
        this.certification = certification;
        this.description = description;
        this.chatStart = chatStart;
        this.address = address;
        this.faq = faq;
        this.phone = phone;
        this.imageKey = imageKey;
        this.user = user;
    }
}

