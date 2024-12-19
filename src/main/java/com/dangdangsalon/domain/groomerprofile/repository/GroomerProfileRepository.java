package com.dangdangsalon.domain.groomerprofile.repository;

import com.dangdangsalon.domain.estimate.entity.EstimateStatus;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import java.util.Optional;
import com.dangdangsalon.domain.mypage.dto.res.BadgeResponseDto;
import com.dangdangsalon.domain.mypage.dto.res.DistrictResponseDto;
import com.dangdangsalon.domain.mypage.dto.res.GroomerRecommendResponseDto;
import com.dangdangsalon.domain.orders.entity.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface GroomerProfileRepository extends JpaRepository<GroomerProfile, Long> {

    @Query("SELECT gp From GroomerProfile gp WHERE gp.user.id = :userId")
    Optional<GroomerProfile> findByUserId(Long userId);

    @Query("SELECT gp FROM GroomerProfile gp " +
            "JOIN FETCH gp.user " +
            "JOIN gp.user.district d " +
            "JOIN d.city c " +
            "WHERE gp.user.id = :userId")
    Optional<GroomerProfile> findByUserIdWithDistrict(@Param("userId") Long userId);

    @Query("SELECT new com.dangdangsalon.domain.mypage.dto.res.DistrictResponseDto" +
            "(d.id, d.name, c.name) " +
            "FROM GroomerServiceArea gsa " +
            "JOIN gsa.district d " +
            "JOIN d.city c " +
            "WHERE gsa.groomerProfile.id = :profileId")
    List<DistrictResponseDto> findServiceAreasWithDistricts(@Param("profileId") Long profileId);

    @Query("SELECT gcs.groomerService.description " +
            "FROM GroomerCanService gcs " +
            "WHERE gcs.groomerProfile.id = :profileId")
    List<String> findGroomerServiceDescriptions(@Param("profileId") Long profileId);

    @Query("SELECT new com.dangdangsalon.domain.mypage.dto.res.BadgeResponseDto" +
            "(b.id, b.name, b.imageKey) " +
            "FROM GroomerBadge gb " +
            "JOIN gb.badge b " +
            "WHERE gb.groomerProfile.id = :profileId")
    List<BadgeResponseDto> findBadgesByProfileId(@Param("profileId") Long profileId);

    @Query("SELECT new com.dangdangsalon.domain.mypage.dto.res.GroomerRecommendResponseDto( " +
            "gp.id, gp.name, gp.imageKey, c.name, d.name) " +
            "FROM GroomerProfile gp " +
            "JOIN gp.user u " +
            "JOIN gp.estimates e " +
            "JOIN u.district d " +
            "JOIN d.city c " +
            "WHERE e.status = :status " +
            "GROUP BY gp.id, gp.name, gp.imageKey, c.name, d.name " +
            "ORDER BY COUNT(e) DESC")
    List<GroomerRecommendResponseDto> findTop5ByAcceptedOrdersWithDto(
            @Param("status") EstimateStatus status,
            Pageable pageable);

    @Query("SELECT new com.dangdangsalon.domain.mypage.dto.res.GroomerRecommendResponseDto( " +
            "gp.id, gp.name, gp.imageKey, c.name, d.name) " +
            "FROM GroomerProfile gp " +
            "JOIN gp.user u " +
            "JOIN u.district d " +
            "JOIN d.city c " +
            "LEFT JOIN gp.reviews r " +
            "WHERE d.name = :districtName " +
            "GROUP BY gp.id, gp.name, gp.imageKey, c.name, d.name " +
            "ORDER BY AVG(r.starScore) DESC, COUNT(r.id) DESC")
    List<GroomerRecommendResponseDto> findTop5GroomersInArea(
            @Param("districtName") String districtName,
            Pageable pageable);

    boolean existsByName(String name);
}
