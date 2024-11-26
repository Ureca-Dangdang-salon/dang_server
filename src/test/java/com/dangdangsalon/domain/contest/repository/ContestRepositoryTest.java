package com.dangdangsalon.domain.contest.repository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import com.dangdangsalon.domain.contest.entity.Contest;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class ContestRepositoryTest {

    @Autowired
    private ContestRepository contestRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("가장 최근의 Contest 조회 테스트")
    void testFindTopContest() {
        Contest beforeContest = Contest.builder()
                .title("이전 콘테스트")
                .description("이전 콘테스트야")
                .startedAt(LocalDateTime.now().minusDays(10))
                .endAt(LocalDateTime.now().minusDays(5))
                .build();

        em.persist(beforeContest);

        Contest recentContest = Contest.builder()
                .title("최신 콘테스트")
                .description("최신 콘테스트야")
                .startedAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusDays(5))
                .build();

        em.persist(recentContest);

        Optional<Contest> result = contestRepository.findTopByOrderByStartedAtDesc();

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("최신 콘테스트");
        assertThat(result.get().getStartedAt()).isAfter(beforeContest.getStartedAt());
    }

    @Test
    @DisplayName("직전 콘테스트 조회 테스트")
    void testFindPreviousContest() {
        Contest beforeContest = Contest.builder()
                .title("이전 콘테스트")
                .description("이전 콘테스트야")
                .startedAt(LocalDateTime.now().minusDays(10))
                .endAt(LocalDateTime.now().minusDays(5))
                .build();

        em.persist(beforeContest);

        Contest recentContest = Contest.builder()
                .title("최신 콘테스트")
                .description("최신 콘테스트야")
                .startedAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusDays(5))
                .build();

        em.persist(recentContest);

        Optional<Contest> result = contestRepository.findPreviousContest();

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("이전 콘테스트");
        assertThat(result.get().getStartedAt()).isBefore(recentContest.getStartedAt());
    }
}