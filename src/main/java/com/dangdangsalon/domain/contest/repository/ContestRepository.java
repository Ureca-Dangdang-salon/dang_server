package com.dangdangsalon.domain.contest.repository;

import com.dangdangsalon.domain.contest.entity.Contest;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ContestRepository extends JpaRepository<Contest, Long> {

    Optional<Contest> findTopByOrderByStartedAtDesc();

    @Query(value = """
    SELECT * 
    FROM contest c
    ORDER BY c.started_at DESC
    LIMIT 1 OFFSET 1
    """, nativeQuery = true)
    Optional<Contest> findPreviousContest();
}
