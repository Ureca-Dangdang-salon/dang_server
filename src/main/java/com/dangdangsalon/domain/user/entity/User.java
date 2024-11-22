package com.dangdangsalon.domain.user.entity;

import com.dangdangsalon.config.base.BaseEntity;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private District district;

    @Builder
    public User(Long id, String username, String name, String email, String imageKey, Role role, District district) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.email = email;
        this.imageKey = imageKey;
        this.role = role;
        this.district = district;
    }

    public void updateAdditionalInfo(Role role, District district) {
        this.role = role;
        this.district = district;
    }
}
