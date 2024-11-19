package com.dangdangsalon.domain.dogprofile.repository;


import com.dangdangsalon.domain.dogprofile.entity.DogProfile;
import com.dangdangsalon.domain.dogprofile.entity.DogProfileFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DogProfileFeatureRepository extends JpaRepository<DogProfileFeature, Long> {

    @Query("SELECT dpf FROM DogProfileFeature dpf " +
            "JOIN FETCH dpf.feature " +
            "WHERE dpf.dogProfile = :dogProfile")
    Optional<List<DogProfileFeature>> findByDogProfile(@Param("dogProfile") DogProfile dogProfile);
}
