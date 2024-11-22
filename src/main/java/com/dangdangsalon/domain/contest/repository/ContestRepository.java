package com.dangdangsalon.domain.contest.repository;

import com.dangdangsalon.domain.contest.entity.Contest;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ContestRepository extends JpaRepository<Contest, Long> {

    Optional<Contest> findTopByOrderByStartedAtDesc();

    @Query("SELECT c FROM Contest c WHERE c.startedAt < (SELECT MAX(c2.startedAt) FROM Contest c2) ORDER BY c.startedAt DESC")
    Optional<Contest> findPreviousContest();
}
