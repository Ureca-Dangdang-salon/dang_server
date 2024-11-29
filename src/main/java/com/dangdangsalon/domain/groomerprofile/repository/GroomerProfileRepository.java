package com.dangdangsalon.domain.groomerprofile.repository;

import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.mypage.dto.res.BadgeResponseDto;
import com.dangdangsalon.domain.mypage.dto.res.DistrictResponseDto;
import com.dangdangsalon.domain.mypage.dto.res.GroomerServicesResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface GroomerProfileRepository extends JpaRepository<GroomerProfile, Long> {

    @Query("SELECT gp FROM GroomerProfile gp " +
            "JOIN FETCH gp.user " +
            "JOIN gp.user.district d " +
            "JOIN d.city c " +
            "WHERE gp.user.id = :userId")
    Optional<GroomerProfile> findByUserIdWithDistrict(@Param("userId") Long userId);

    @Query("SELECT new com.dangdangsalon.domain.mypage.dto.res.DistrictResponseDto" +
            "(d.name, c.name) " +
            "FROM GroomerServiceArea gsa " +
            "JOIN gsa.district d " +
            "JOIN d.city c " +
            "WHERE gsa.groomerProfile.id = :profileId")
    List<DistrictResponseDto> findServiceAreasWithDistricts(@Param("profileId") Long profileId);

    @Query("SELECT new com.dangdangsalon.domain.mypage.dto.res.GroomerServicesResponseDto" +
            "(gcs.groomerService.description, gcs.groomerService.isCustom) " +
            "FROM GroomerCanService gcs " +
            "WHERE gcs.groomerProfile.id = :profileId")
    List<GroomerServicesResponseDto> findGroomerServices(@Param("profileId") Long profileId);

    @Query("SELECT new com.dangdangsalon.domain.mypage.dto.res.BadgeResponseDto" +
            "(b.id, b.name, b.imageKey) " +
            "FROM GroomerBadge gb " +
            "JOIN gb.badge b " +
            "WHERE gb.groomerProfile.id = :profileId")
    List<BadgeResponseDto> findBadgesByProfileId(@Param("profileId") Long profileId);

}
