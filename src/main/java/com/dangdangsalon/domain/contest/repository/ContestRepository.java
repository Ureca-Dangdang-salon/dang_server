package com.dangdangsalon.domain.contest.repository;

import com.dangdangsalon.domain.contest.entity.Contest;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestRepository extends JpaRepository<Contest, Long> {

    Optional<Contest> findTopByOrderByStartedAtDesc();
}
