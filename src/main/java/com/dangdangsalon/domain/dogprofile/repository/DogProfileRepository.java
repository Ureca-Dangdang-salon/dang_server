package com.dangdangsalon.domain.dogprofile.repository;

import com.dangdangsalon.domain.dogprofile.entity.DogProfile;
import com.dangdangsalon.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DogProfileRepository extends JpaRepository<DogProfile, Long> {
    Optional<List<DogProfile>> findByUser(User user);

    @Query("SELECT dp FROM DogProfile dp " +
            "LEFT JOIN FETCH dp.dogProfileFeatures dpf " +
            "LEFT JOIN FETCH dpf.feature " +
            "WHERE dp.id = :dogProfileId AND dp.user.id = :userId")
    Optional<DogProfile> findByIdAndUserIdWithFeatures(@Param("dogProfileId") Long dogProfileId,
                                                       @Param("userId") Long userId);

}
