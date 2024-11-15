package com.dangdangsalon.domain.groomerprofile.entity;

import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "groomer_profile_pictures")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroomerPictures {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_key")
    private String imageKey;

    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private GroomerProfile groomerProfile;

    @Builder
    public GroomerPictures(String imageKey, GroomerProfile groomerProfile) {
        this.imageKey = imageKey;
        this.groomerProfile = groomerProfile;
    }
}
