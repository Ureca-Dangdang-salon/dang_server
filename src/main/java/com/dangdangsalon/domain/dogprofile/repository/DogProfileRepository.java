package com.dangdangsalon.domain.dogprofile.repository;

import com.dangdangsalon.domain.dogprofile.entity.DogProfile;
import com.dangdangsalon.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DogProfileRepository extends JpaRepository<DogProfile, Long> {
    Optional<List<DogProfile>> findByUser(User user);
}
