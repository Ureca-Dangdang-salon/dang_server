package com.dangdangsalon.domain.estimate.entity;

import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "estimate_picture")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EstimatePicture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "key")
    private Long id;

    @Column(name = "image_key")
    private String imageKey;

    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private GroomerProfile groomerProfile;

    @Builder
    public EstimatePicture(String imageKey, GroomerProfile groomerProfile) {
        this.imageKey = imageKey;
        this.groomerProfile = groomerProfile;
    }
}
