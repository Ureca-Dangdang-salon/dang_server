package com.dangdangsalon.domain.contest.repository;

import com.dangdangsalon.domain.contest.entity.ContestPostLike;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContestPostLikeRepository extends JpaRepository<ContestPostLike, Long> {

    @Query("SELECT pl.contestPost.id FROM ContestPostLike pl WHERE pl.user.id = :userId AND pl.contestPost.id IN :postIds")
    List<Long> findLikedPostIdsByUserId(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);
}
