package com.dangdangsalon.domain.orders.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.orders.entity.OrderStatus;
import com.dangdangsalon.domain.orders.entity.Orders;
import com.dangdangsalon.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

@DataJpaTest
@ActiveProfiles("test")
class OrdersRepositoryTest {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("견적서 ID로 주문 조회 테스트")
    void testFindByEstimateId() {
        // Given
        Estimate estimate = Estimate.builder()
                .build();

        em.persist(estimate);

        Orders order = Orders.builder()
                .orderName("Test Order")
                .estimate(estimate)
                .amountValue(20000)
                .status(OrderStatus.PENDING)
                .tossOrderId("TOSS_ORDER_123")
                .build();

        em.persist(order);
        em.flush();

        // When
        Optional<Orders> result = ordersRepository.findByEstimateId(estimate.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.get().getEstimate()).isEqualTo(estimate);
    }

    @Test
    @DisplayName("Toss Order ID로 주문 조회 테스트")
    void testFindByTossOrderId() {
        // Given
        Orders order = Orders.builder()
                .tossOrderId("TOSS_ORDER_456")
                .amountValue(15000)
                .status(OrderStatus.ACCEPTED)
                .build();

        em.persist(order);
        em.flush();

        // When
        Optional<Orders> result = ordersRepository.findByTossOrderId("TOSS_ORDER_456");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTossOrderId()).isEqualTo("TOSS_ORDER_456");
        assertThat(result.get().getStatus()).isEqualTo(OrderStatus.ACCEPTED);
    }

    @Test
    @DisplayName("사용자와 상태로 주문 조회 테스트")
    void testFindAllByUserIdAndStatus() {
        // Given
        User user = User.builder()
                .name("이민수")
                .build();


        em.persist(user);

        Orders order1 = Orders.builder()
                .user(user)
                .amountValue(10000)
                .status(OrderStatus.ACCEPTED)
                .tossOrderId("TOSS_ORDER_789")
                .build();

        Orders order2 = Orders.builder()
                .user(user)
                .amountValue(20000)
                .status(OrderStatus.ACCEPTED)
                .tossOrderId("TOSS_ORDER_101")
                .build();

        Orders order3 = Orders.builder()
                .user(user)
                .amountValue(15000)
                .status(OrderStatus.REJECTED)
                .tossOrderId("TOSS_ORDER_102")
                .build();

        em.persist(order1);
        em.persist(order2);
        em.persist(order3);
        em.flush();

        // When
        Optional<List<Orders>> result = ordersRepository.findAllByUserIdAndStatus(user.getId(), OrderStatus.ACCEPTED);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(2);
        assertThat(result.get())
                .extracting("tossOrderId")
                .containsExactlyInAnyOrder("TOSS_ORDER_789", "TOSS_ORDER_101");
    }


    @Test
    @DisplayName("존재하지 않는 견적으로 조회 테스트")
    void testFindByEstimateId_NotFound() {
        // When
        Optional<Orders> result = ordersRepository.findByEstimateId(999L);

        // Then
        assertThat(result).isNotPresent();
    }

    @Test
    @DisplayName("존재하지 않는 Toss Order ID 조회 테스트")
    void testFindByTossOrderId_NotFound() {
        // When
        Optional<Orders> result = ordersRepository.findByTossOrderId("INVALID_ORDER_ID");

        // Then
        assertThat(result).isNotPresent();
    }

    @Test
    @DisplayName("존재하지 않는 사용자와 상태로 조회 테스트")
    void testFindAllByUserIdAndStatus_NotFound() {
        // When
        Optional<List<Orders>> result = ordersRepository.findAllByUserIdAndStatus(99L, OrderStatus.PENDING);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEmpty();
    }
}
