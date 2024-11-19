package com.dangdangsalon.domain.dogprofile.repository;


import com.dangdangsalon.domain.dogprofile.entity.DogProfile;
import com.dangdangsalon.domain.dogprofile.entity.DogProfileFeature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DogProfileFeatureRepository extends JpaRepository<DogProfileFeature, Long> {
    Optional<List<DogProfileFeature>> findByDogProfile(DogProfile dogProfile);
}
