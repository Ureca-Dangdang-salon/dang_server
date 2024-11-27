package com.dangdangsalon.domain.contest.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.dangdangsalon.domain.contest.entity.Contest;
import com.dangdangsalon.domain.contest.entity.ContestPost;
import com.dangdangsalon.domain.contest.entity.ContestPostLike;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class ContestPostLikeRepositoryTest {

    @Autowired
    private ContestPostLikeRepository contestPostLikeRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("게시글의 좋아요 수 조회 테스트")
    void testGetLikeCount() {
        Contest contest = Contest.builder()
                .title("콘테스트")
                .startedAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusDays(5))
                .build();

        em.persist(contest);

        User user1 = User.builder().name("User 1").username("user1").build();
        User user2 = User.builder().name("User 2").username("user2").build();

        em.persist(user1);
        em.persist(user2);

        GroomerProfile groomerProfile = GroomerProfile.builder().name("groomer").build();
        em.persist(groomerProfile);

        ContestPost post = ContestPost.builder()
                .dogName("Dog 1")
                .contest(contest)
                .groomerProfile(groomerProfile)
                .user(user1)
                .build();
        em.persist(post);

        em.persist(ContestPostLike.builder().user(user1).contestPost(post).build());
        em.persist(ContestPostLike.builder().user(user2).contestPost(post).build());

        Long likeCount = contestPostLikeRepository.getLikeCountByPostId(post.getId());

        assertThat(likeCount).isEqualTo(2);
    }

    @Test
    @DisplayName("좋아요 중복 확인 테스트")
    void testDuplicateLike() {
        User user = User.builder().name("User").username("user").build();
        em.persist(user);

        Contest contest = Contest.builder()
                .title("Contest")
                .startedAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusDays(5))
                .build();
        em.persist(contest);

        GroomerProfile groomerProfile = GroomerProfile.builder().name("groomer").build();
        em.persist(groomerProfile);

        ContestPost post = ContestPost.builder()
                .dogName("Dog")
                .contest(contest)
                .groomerProfile(groomerProfile)
                .user(user)
                .build();
        em.persist(post);

        em.persist(ContestPostLike.builder().user(user).contestPost(post).build());

        boolean exists = contestPostLikeRepository.existsByUserIdAndContestPostId(user.getId(), post.getId());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("좋아요 취소 테스트")
    void testCancelLike() {
        User user = User.builder().name("User").username("user").build();
        em.persist(user);

        Contest contest = Contest.builder()
                .title("Contest")
                .startedAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusDays(5))
                .build();
        em.persist(contest);

        GroomerProfile groomerProfile = GroomerProfile.builder().name("groomer").build();
        em.persist(groomerProfile);

        ContestPost post = ContestPost.builder()
                .dogName("Dog")
                .contest(contest)
                .groomerProfile(groomerProfile)
                .user(user)
                .build();
        em.persist(post);

        em.persist(ContestPostLike.builder().user(user).contestPost(post).build());

        contestPostLikeRepository.deleteByUserIdAndContestPostId(user.getId(), post.getId());

        boolean exists = contestPostLikeRepository.existsByUserIdAndContestPostId(user.getId(), post.getId());

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 게시글의 좋아요 수 조회 테스트")
    void testGetLikeCountNotExistPost() {
        Long likeCount = contestPostLikeRepository.getLikeCountByPostId(999L);

        assertThat(likeCount).isEqualTo(0L);
    }
}