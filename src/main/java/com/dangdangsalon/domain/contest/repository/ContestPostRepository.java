package com.dangdangsalon.domain.contest.repository;

import com.dangdangsalon.domain.contest.dto.PostRankDto;
import com.dangdangsalon.domain.contest.entity.ContestPost;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContestPostRepository extends JpaRepository<ContestPost, Long> {

    @Query("SELECT p FROM ContestPost p WHERE p.contest.id = :contestId ORDER BY p.createdAt DESC")
    Page<ContestPost> findByContestId(Long contestId, Pageable pageable);

    boolean existsByContestIdAndUserId(Long contestId, Long userId);

    @Query(value = "SELECT new com.dangdangsalon.domain.contest.dto.PostRankDto(p.id, p.user.id, p.dogName, p.imageKey, COUNT(pl)) " +
            "FROM ContestPost p LEFT JOIN ContestPostLike pl ON p.id = pl.contestPost.id " +
            "JOIN p.groomerProfile gp " +
            "WHERE p.contest.id = :contestId " +
            "GROUP BY p.id, p.user.id, gp.name, p.imageKey " +
            "ORDER BY COUNT(pl) DESC, p.createdAt ASC")
    Page<PostRankDto> findTopRankPostsByContestId(@Param("contestId") Long contestId, Pageable pageable);

    @Query("SELECT p FROM ContestPost p " +
            "LEFT JOIN ContestPostLike l ON p.id = l.contestPost.id " +
            "WHERE p.contest.id = :contestId " +
            "GROUP BY p.id " +
            "ORDER BY COUNT(l) DESC, p.createdAt ASC")
    List<ContestPost> findTopLikedPostByContestId(@Param("contestId") Long contestId);
}
