package com.dangdangsalon.domain.dogprofile.entity;

import com.dangdangsalon.domain.dogprofile.feature.entity.Feature;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "dog_profile_feature")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DogProfileFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id")
    private Feature feature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private DogProfile dogProfile;

    @Builder
    public DogProfileFeature(Feature feature, DogProfile dogProfile) {
        this.feature = feature;
        this.dogProfile = dogProfile;
    }
}