//package com.dangdangsalon.domain.coupon.repository;
//
//import org.junit.jupiter.api.Test;
//
//
//import com.dangdangsalon.domain.coupon.entity.CouponEvent;
//import org.junit.jupiter.api.DisplayName;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DataJpaTest
//@ActiveProfiles("test")
//class CouponEventRepositoryTest {
//
//    @Autowired
//    private CouponEventRepository couponEventRepository;
//
//    @Test
//    @DisplayName("활성화된 쿠폰 이벤트 정상 조회")
//    void testFindActiveEvent() {
//        // Given: 테스트 데이터 생성
//        CouponEvent activeEvent = CouponEvent.builder()
//                .name("활성화된 이벤트")
//                .startedAt(LocalDateTime.now().minusDays(1))
//                .endedAt(LocalDateTime.now().plusDays(1))
//                .build();
//
//        CouponEvent pastEvent = CouponEvent.builder()
//                .name("종료된 이벤트")
//                .startedAt(LocalDateTime.now().minusDays(5))
//                .endedAt(LocalDateTime.now().minusDays(1))
//                .build();
//
//        CouponEvent futureEvent = CouponEvent.builder()
//                .name("시작되지 않은 이벤트")
//                .startedAt(LocalDateTime.now().plusDays(1))
//                .endedAt(LocalDateTime.now().plusDays(5))
//                .build();
//
//        couponEventRepository.save(activeEvent);
//        couponEventRepository.save(pastEvent);
//        couponEventRepository.save(futureEvent);
//
//        // When: 현재 시간 기준 활성 이벤트 조회
//        LocalDateTime now = LocalDateTime.now();
//        List<CouponEvent> activeEvents = couponEventRepository.findActiveEvents(now);
//
//        // Then: 활성 이벤트만 조회되었는지 검증
//        assertThat(activeEvents).hasSize(1);
//        assertThat(activeEvents.get(0).getName()).isEqualTo("활성화된 이벤트");
//    }
//}