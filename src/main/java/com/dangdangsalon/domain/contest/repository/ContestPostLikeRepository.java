package com.dangdangsalon.domain.contest.repository;

import com.dangdangsalon.domain.contest.entity.ContestPostLike;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContestPostLikeRepository extends JpaRepository<ContestPostLike, Long> {

    @Query("SELECT COUNT(pl) FROM ContestPostLike pl WHERE pl.contestPost.id = :postId")
    Long getLikeCountByPostId(@Param("postId") Long postId);

    boolean existsByUserIdAndContestPostId(Long userId, Long postId);

    void deleteByUserIdAndContestPostId(Long userId, Long postId);

    @Query("SELECT cpl.contestPost.id FROM ContestPostLike cpl WHERE cpl.user.id = :userId AND cpl.contestPost.id IN :postIds")
    List<Long> findLikedPostIds(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);

}
