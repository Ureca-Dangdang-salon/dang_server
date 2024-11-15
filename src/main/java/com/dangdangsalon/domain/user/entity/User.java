package com.dangdangsalon.domain.user.entity;

import com.dangdangsalon.domain.region.entity.District;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    @Column(name = "image_key")
    private String imageKey;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne
    @JoinColumn(name = "district_id")
    private District district;

    @Builder
    public User(String name, String email, String imageKey, Role role, District district) {
        this.name = name;
        this.email = email;
        this.imageKey = imageKey;
        this.role = role;
        this.district = district;
    }
}
