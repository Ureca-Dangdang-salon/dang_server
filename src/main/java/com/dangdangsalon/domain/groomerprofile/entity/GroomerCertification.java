package com.dangdangsalon.domain.groomerprofile.entity;

import com.dangdangsalon.config.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "groomer_certification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroomerCertification extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String certification;

    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private GroomerProfile groomerProfile;

    @Builder
    public GroomerCertification(String certification, GroomerProfile groomerProfile) {
        this.certification = certification;
        this.groomerProfile = groomerProfile;
    }

}