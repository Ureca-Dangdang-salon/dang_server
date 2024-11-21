package com.dangdangsalon.domain.groomerprofile.repository;

import com.dangdangsalon.domain.groomerprofile.entity.GroomerCanService;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroomerCanServiceRepository extends JpaRepository<GroomerCanService, Integer> {
    List<GroomerCanService> findByGroomerProfile(GroomerProfile groomerProfile);
}
