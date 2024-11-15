package com.dangdangsalon.domain.dogprofile.entity;

import com.dangdangsalon.domain.feature.entity.Feature;
import jakarta.persistence.*;



@Table(name = "dog_profile_feature")
public class DogProfileFeature {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id")
    private Feature feature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    private DogProfile dogProfile;

}