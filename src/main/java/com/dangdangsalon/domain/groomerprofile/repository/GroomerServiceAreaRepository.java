package com.dangdangsalon.domain.groomerprofile.repository;

import com.dangdangsalon.domain.groomerprofile.entity.GroomerServiceArea;
import com.dangdangsalon.domain.region.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroomerServiceAreaRepository extends JpaRepository<GroomerServiceArea, Long> {
    List<GroomerServiceArea> findByDistrict(District district);
}
