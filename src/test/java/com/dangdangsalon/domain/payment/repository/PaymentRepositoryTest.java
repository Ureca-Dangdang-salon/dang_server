package com.dangdangsalon.domain.payment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.dangdangsalon.domain.orders.entity.OrderStatus;
import com.dangdangsalon.domain.orders.entity.Orders;
import com.dangdangsalon.domain.payment.entity.Payment;
import com.dangdangsalon.domain.payment.entity.PaymentStatus;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("findByPaymentKey - 특정 결제 키로 결제 조회 테스트")
    void testFindByPaymentKey() {
        // Given
        Orders order = Orders.builder()
                .amountValue(10000)
                .status(OrderStatus.PENDING)
                .tossOrderId("TOSS_ORDER_123")
                .build();

        em.persist(order);

        Payment payment = Payment.builder()
                .paymentKey("PAYMENT_KEY_123")
                .totalAmount(10000)
                .paymentStatus(PaymentStatus.ACCEPTED)
                .orders(order)
                .build();

        em.persist(payment);
        em.flush();

        // When
        Optional<Payment> result = paymentRepository.findByPaymentKey("PAYMENT_KEY_123");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getPaymentKey()).isEqualTo("PAYMENT_KEY_123");
        assertThat(result.get().getTotalAmount()).isEqualTo(10000);
        assertThat(result.get().getOrders()).isEqualTo(order);
    }

    @Test
    @DisplayName("findByOrders - 특정 주문으로 결제 조회 테스트")
    void testFindByOrders() {
        // Given
        Orders order = Orders.builder()
                .amountValue(20000)
                .status(OrderStatus.ACCEPTED)
                .tossOrderId("TOSS_ORDER_456")
                .build();

        em.persist(order);

        Payment payment = Payment.builder()
                .paymentKey("PAYMENT_KEY_456")
                .totalAmount(20000)
                .paymentStatus(PaymentStatus.ACCEPTED)
                .orders(order)
                .build();

        em.persist(payment);
        em.flush();

        // When
        Optional<Payment> result = paymentRepository.findByOrders(order);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getPaymentKey()).isEqualTo("PAYMENT_KEY_456");
        assertThat(result.get().getTotalAmount()).isEqualTo(20000);
        assertThat(result.get().getPaymentStatus()).isEqualTo(PaymentStatus.ACCEPTED);
    }

    @Test
    @DisplayName("findByPaymentKey - 존재하지 않는 결제 키 조회 테스트")
    void testFindByPaymentKey_NotFound() {
        // When
        Optional<Payment> result = paymentRepository.findByPaymentKey("INVALID_KEY");

        // Then
        assertThat(result).isNotPresent();
    }

    @Test
    @DisplayName("findByOrders - 존재하지 않는 주문으로 결제 조회 테스트")
    void testFindByOrders_NotFound() {
        // Given
        Orders order = Orders.builder()
                .amountValue(15000)
                .status(OrderStatus.PENDING)
                .tossOrderId("TOSS_ORDER_NOT_EXIST")
                .build();

        em.persist(order);
        em.flush();

        // When
        Optional<Payment> result = paymentRepository.findByOrders(order);

        // Then
        assertThat(result).isNotPresent();
    }
}
