package com.dangdangsalon.domain.groomerprofile.entity;

import com.dangdangsalon.config.base.BaseEntity;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.region.entity.District;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "district_service")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroomerServiceArea extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private GroomerProfile groomerProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private District district;

    @Builder
    public GroomerServiceArea(GroomerProfile groomerProfile, District district) {
        this.groomerProfile = groomerProfile;
        this.district = district;
    }
}
