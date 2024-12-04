package com.dangdangsalon.domain.notification.repository;

import com.dangdangsalon.domain.notification.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    Optional<FcmToken> findByUserId(Long userId);
    Optional<FcmToken> findByFcmToken(String token);
    void deleteByFcmToken(String fcmToken);
}
