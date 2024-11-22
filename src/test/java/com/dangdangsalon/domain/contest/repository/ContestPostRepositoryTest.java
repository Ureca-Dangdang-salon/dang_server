package com.dangdangsalon.domain.contest.repository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import com.dangdangsalon.domain.contest.dto.PostRankDto;
import com.dangdangsalon.domain.contest.entity.Contest;
import com.dangdangsalon.domain.contest.entity.ContestPost;
import com.dangdangsalon.domain.contest.entity.ContestPostLike;
import com.dangdangsalon.domain.groomerprofile.entity.GroomerProfile;
import com.dangdangsalon.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class ContestPostRepositoryTest {

    @Autowired
    private ContestPostRepository contestPostRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("콘테스트 게시글 페이징 조회 테스트")
    void testFindByContestId() {
        Contest contest = Contest.builder()
                .title("콘테스트")
                .startedAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusDays(5))
                .build();

        em.persist(contest);

        GroomerProfile groomerProfile = GroomerProfile.builder()
                .name("groomer")
                .build();
        em.persist(groomerProfile);

        for (int i = 0; i < 5; i++) {
            User user = User.builder()
                    .name("user" + i)
                    .build();
            em.persist(user);

            em.persist(
                    ContestPost.builder()
                            .dogName("Dog" + i)
                            .description("Description" + i)
                            .contest(contest)
                            .groomerProfile(groomerProfile)
                            .user(user)
                            .build()
            );
        }

        Pageable pageable = PageRequest.of(0, 3);

        Page<ContestPost> result = contestPostRepository.findByContestId(contest.getId(), pageable);

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(5);
    }

    @Test
    @DisplayName("유저가 콘테스트에 참여했는지 여부 테스트")
    void testExistsByContestIdAndUserId() {
        User user = User.builder().name("user1").build();
        em.persist(user);

        Contest contest = Contest.builder()
                .title("콘테스트")
                .startedAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusDays(5))
                .build();
        em.persist(contest);

        GroomerProfile groomerProfile = GroomerProfile.builder()
                .name("groomer")
                .build();
        em.persist(groomerProfile);

        ContestPost contestPost = ContestPost.builder()
                .dogName("멍멍")
                .contest(contest)
                .groomerProfile(groomerProfile)
                .user(user)
                .build();
        em.persist(contestPost);

        boolean result = contestPostRepository.existsByContestIdAndUserId(contest.getId(), user.getId());

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("좋아요를 가장 많이 받은 게시글 조회 테스트")
    void testFindTopLikedPost() {
        Contest contest = Contest.builder()
                .title("콘테스트")
                .startedAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusDays(5))
                .build();
        em.persist(contest);

        GroomerProfile groomerProfile = GroomerProfile.builder()
                .name("groomer")
                .build();
        em.persist(groomerProfile);

        for (int i = 1; i <= 10; i++) {
            User user = User.builder().name("User " + i).username("user" + i).build();
            em.persist(user);

            ContestPost post = ContestPost.builder()
                    .dogName("Dog " + i)
                    .contest(contest)
                    .groomerProfile(groomerProfile)
                    .user(user)
                    .build();

            em.persist(post);

            for (int j = 0; j < i; j++) {
                User liker = User.builder().name("Liker " + j).username("liker" + j).build();
                em.persist(liker);
                em.persist(ContestPostLike.builder().user(liker).contestPost(post).build());
            }
        }

        Pageable pageable = PageRequest.of(0, 6);
        Page<PostRankDto> page = contestPostRepository.findTopRankPostsByContestId(contest.getId(), pageable);
        List<PostRankDto> result = page.getContent();

        assertThat(result).hasSize(6);
        assertThat(result.get(1).getDogName()).isEqualTo("Dog 9");
        assertThat(result.get(5).getDogName()).isEqualTo("Dog 5");
    }

    @Test
    @DisplayName("특정 Contest의 순위 게시글 조회")
    void testFindTopRankPostsByContestId() {
        Contest contest = Contest.builder()
                .title("Contest 4")
                .startedAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusDays(5))
                .build();

        em.persist(contest);

        User user1 = User.builder().name("User 1").username("user1").build();
        User user2 = User.builder().name("User 2").username("user2").build();
        User user3 = User.builder().name("User 3").username("user3").build();

        em.persist(user1);
        em.persist(user2);
        em.persist(user3);

        GroomerProfile groomerProfile = GroomerProfile.builder()
                .name("groomer")
                .build();
        em.persist(groomerProfile);

        ContestPost post1 = ContestPost.builder()
                .dogName("Dog 1")
                .groomerProfile(groomerProfile)
                .user(user1)
                .contest(contest)
                .build();

        ContestPost post2 = ContestPost.builder()
                .dogName("Dog 2")
                .groomerProfile(groomerProfile)
                .user(user2)
                .contest(contest)
                .build();

        em.persist(post1);
        em.persist(post2);

        em.persist(ContestPostLike.builder().user(user1).contestPost(post1).build());
        em.persist(ContestPostLike.builder().user(user2).contestPost(post2).build());
        em.persist(ContestPostLike.builder().user(user3).contestPost(post2).build());

        ContestPost result = contestPostRepository.findTopLikedPostByContestId(contest.getId()).get(0);

        assertThat(result.getDogName()).isEqualTo("Dog 2");
    }
}