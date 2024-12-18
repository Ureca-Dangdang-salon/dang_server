package com.dangdangsalon.domain.notification.repository;

import com.dangdangsalon.domain.notification.entity.FcmToken;
import com.dangdangsalon.domain.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    List<FcmToken> findByUserId(Long userId);
    Optional<FcmToken> findByFcmToken(String token);
    void deleteByFcmToken(String fcmToken);

    @Query("SELECT f.fcmToken FROM FcmToken f WHERE f.user.role = :role")
    List<String> findAllByUserRole(@Param("role") Role role);
}
