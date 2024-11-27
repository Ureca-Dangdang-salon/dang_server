package com.dangdangsalon.domain.payment.service;

import com.dangdangsalon.domain.dogprofile.entity.DogProfile;
import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestProfiles;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequestService;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestServiceRepository;
import com.dangdangsalon.domain.groomerservice.entity.GroomerService;
import com.dangdangsalon.domain.orders.entity.OrderStatus;
import com.dangdangsalon.domain.orders.entity.Orders;
import com.dangdangsalon.domain.orders.repository.OrdersRepository;
import com.dangdangsalon.domain.payment.dto.PaymentResponseDto;
import com.dangdangsalon.domain.payment.entity.Payment;
import com.dangdangsalon.domain.payment.entity.PaymentStatus;
import com.dangdangsalon.domain.payment.repository.PaymentRepository;
import com.dangdangsalon.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentGetServiceTest {

    @Mock
    private OrdersRepository ordersRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private EstimateRequestServiceRepository estimateRequestServiceRepository;

    @InjectMocks
    private PaymentGetService paymentGetService;

    private User mockUser;
    private Orders mockOrder;
    private Payment mockPayment;
    private EstimateRequest mockEstimateRequest;
    private EstimateRequestProfiles mockEstimateRequestProfile;
    private DogProfile mockDogProfile;
    private EstimateRequestService mockEstimateRequestService;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .name("이민수")
                .build();
        ReflectionTestUtils.setField(mockUser, "id", 1L);

        GroomerService mockGroomerService = GroomerService.builder()
                .description("목욕 서비스")
                .build();
        ReflectionTestUtils.setField(mockGroomerService, "id", 1L);

        mockDogProfile = DogProfile.builder()
                .name("멍이")
                .build();
        ReflectionTestUtils.setField(mockDogProfile, "id", 1L);

        mockEstimateRequestProfile = EstimateRequestProfiles.builder()
                .dogProfile(mockDogProfile)
                .aggressionCharge(1000)
                .healthIssueCharge(2000)
                .build();
        ReflectionTestUtils.setField(mockEstimateRequestProfile, "id", 1L);

        mockEstimateRequestService = EstimateRequestService.builder()
                .groomerService(mockGroomerService)
                .price(10000)
                .build();

        mockEstimateRequest = EstimateRequest.builder()
                .estimateRequestProfiles(List.of(mockEstimateRequestProfile))
                .build();
        ReflectionTestUtils.setField(mockEstimateRequest, "id", 1L);

        mockOrder = Orders.builder()
                .user(mockUser)
                .status(OrderStatus.ACCEPTED)
                .estimate(Estimate.builder().estimateRequest(mockEstimateRequest).build())
                .build();
        ReflectionTestUtils.setField(mockOrder, "id", 1L);

        mockPayment = Payment.builder()
                .orders(mockOrder)
                .requestedAt(LocalDateTime.now())
                .totalAmount(15000)
                .paymentStatus(PaymentStatus.ACCEPTED)
                .build();
        ReflectionTestUtils.setField(mockPayment, "id", 1L);
    }

    @Test
    @DisplayName("사용자의 결제 내역 조회")
    void getPayments_Success() {
        // Given
        given(ordersRepository.findAllByUserIdAndStatus(anyLong(), any(OrderStatus.class)))
                .willReturn(Optional.of(List.of(mockOrder)));

        given(paymentRepository.findByOrders(any(Orders.class)))
                .willReturn(Optional.of(mockPayment));

        given(estimateRequestServiceRepository.findByEstimateRequestProfilesId(anyLong()))
                .willReturn(List.of(mockEstimateRequestService));

        // When
        List<PaymentResponseDto> paymentList = paymentGetService.getPayments(mockUser.getId());

        // Then
        assertThat(paymentList).isNotNull();
        assertThat(paymentList).hasSize(1);

        PaymentResponseDto payment = paymentList.get(0);
        assertThat(payment.getTotalAmount()).isEqualTo(15000);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.ACCEPTED.toString());
        assertThat(payment.getDogProfileList()).hasSize(1);

        var dogProfile = payment.getDogProfileList().get(0);
        assertThat(dogProfile.getDogName()).isEqualTo("멍이");
        assertThat(dogProfile.getAggressionCharge()).isEqualTo(1000);
        assertThat(dogProfile.getHealthIssueCharge()).isEqualTo(2000);
        assertThat(dogProfile.getServicePriceList()).hasSize(1);

        var service = dogProfile.getServicePriceList().get(0);
        assertThat(service.getDescription()).isEqualTo("목욕 서비스");
        assertThat(service.getPrice()).isEqualTo(10000);

        verify(ordersRepository, times(1)).findAllByUserIdAndStatus(anyLong(), any(OrderStatus.class));
        verify(paymentRepository, times(1)).findByOrders(any(Orders.class));
        verify(estimateRequestServiceRepository, times(1)).findByEstimateRequestProfilesId(anyLong());
    }

    @Test
    @DisplayName("결제 완료된 주문이 없는 경우")
    void getPayments_NoPayments() {
        // Given
        given(ordersRepository.findAllByUserIdAndStatus(anyLong(), any(OrderStatus.class)))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> paymentGetService.getPayments(mockUser.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("결제 완료된 주문이 없습니다.");

        verify(ordersRepository, times(1)).findAllByUserIdAndStatus(anyLong(), any(OrderStatus.class));
        verify(paymentRepository, times(0)).findByOrders(any(Orders.class));
        verify(estimateRequestServiceRepository, times(0)).findByEstimateRequestProfilesId(anyLong());
    }

    @Test
    @DisplayName("주문 ID에 대한 결제 정보가 없을 때 예외 발생")
    void getPayments_PaymentNotFound() {
        // Given
        given(ordersRepository.findAllByUserIdAndStatus(anyLong(), any(OrderStatus.class)))
                .willReturn(Optional.of(List.of(mockOrder)));

        given(paymentRepository.findByOrders(any(Orders.class)))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> paymentGetService.getPayments(mockUser.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("주문 ID " + mockOrder.getId() + "에 대한 결제 정보가 없습니다.");

        // Verify
        verify(ordersRepository, times(1)).findAllByUserIdAndStatus(anyLong(), any(OrderStatus.class));
        verify(paymentRepository, times(1)).findByOrders(any(Orders.class));
        verify(estimateRequestServiceRepository, times(0)).findByEstimateRequestProfilesId(anyLong());
    }

}