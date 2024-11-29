package com.dangdangsalon.domain.groomerprofile.entity;

import com.dangdangsalon.config.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "groomer_badge")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroomerBadge extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private GroomerProfile groomerProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id")
    private Badge badge;

    @Builder
    public GroomerBadge(GroomerProfile groomerProfile, Badge badge) {
        this.groomerProfile = groomerProfile;
        this.badge = badge;
    }
}
