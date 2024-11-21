package com.dangdangsalon.domain.contest.repository;

import com.dangdangsalon.domain.contest.entity.ContestPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestPostRepository extends JpaRepository<ContestPost, Long> {

    Page<ContestPost> findByContestId(Long contestId, Pageable pageable);

    boolean existsByContestIdAndUserId(Long contestId, Long userId);
}
