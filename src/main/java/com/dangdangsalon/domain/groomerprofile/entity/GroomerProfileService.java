package com.dangdangsalon.domain.groomerprofile.entity;

import com.dangdangsalon.domain.groomerservice.entity.GroomerService;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "groomer_profile_service")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroomerProfileService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private GroomerProfile groomerProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private GroomerService groomerService;

    @Builder
    public GroomerProfileService(GroomerProfile groomerProfile, GroomerService groomerService) {
        this.groomerProfile = groomerProfile;
        this.groomerService = groomerService;
    }
}
