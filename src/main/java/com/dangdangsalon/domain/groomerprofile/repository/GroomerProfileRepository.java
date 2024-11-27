package com.dangdangsalon.domain.groomerprofile.repository;

import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GroomerProfileRepository extends JpaRepository<GroomerProfile, Long> {

    @Query("SELECT gp From GroomerProfile gp WHERE gp.user.id = :userId")
    Optional<GroomerProfile> findByUserId(Long userId);
}
