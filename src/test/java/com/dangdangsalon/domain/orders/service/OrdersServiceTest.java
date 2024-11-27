package com.dangdangsalon.domain.orders.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.repository.EstimateRepository;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.orders.dto.OrdersRequestDto;
import com.dangdangsalon.domain.orders.dto.OrdersResponseDto;
import com.dangdangsalon.domain.orders.entity.OrderStatus;
import com.dangdangsalon.domain.orders.entity.Orders;
import com.dangdangsalon.domain.orders.repository.OrdersRepository;
import com.dangdangsalon.domain.user.entity.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OrdersServiceTest {

    @Mock
    private OrdersRepository ordersRepository;

    @Mock
    private EstimateRepository estimateRepository;

    @InjectMocks
    private OrdersService ordersService;

    private Estimate mockEstimate;
    private Orders mockOrder;
    private User mockUser;
    private EstimateRequest mockEstimateRequest;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .name("이민수")
                .build();

        mockEstimateRequest = mock(EstimateRequest.class);

        mockEstimate = Estimate.builder()
                .estimateRequest(mockEstimateRequest)
                .build();
        ReflectionTestUtils.setField(mockEstimate, "id", 1L);

        mockOrder = Orders.builder()
                .orderName("미용 견적서")
                .amountValue(10000)
                .status(OrderStatus.PENDING)
                .estimate(mockEstimate)
                .user(mockUser)
                .tossOrderId("TOSS123")
                .build();
    }

    @Test
    @DisplayName("주문 등록 시 새 주문 생성")
    void insertOrdersNewOrder() {
        OrdersRequestDto requestDto = OrdersRequestDto.builder()
                .orderName("미용 견적서")
                .amount(10000)
                .tossOrderId("TOSS123")
                .build();

        given(estimateRepository.findById(anyLong())).willReturn(Optional.of(mockEstimate));
        given(ordersRepository.findByEstimateId(anyLong())).willReturn(Optional.empty());
        given(ordersRepository.save(any(Orders.class))).willReturn(mockOrder);

        OrdersResponseDto response = ordersService.insertOrders(1L, requestDto);

        assertThat(response).isNotNull();
        assertThat(response.getOrderName()).isEqualTo("미용 견적서");
        assertThat(response.getAmount()).isEqualTo(10000);
        assertThat(response.getTossOrderId()).isEqualTo("TOSS123");

        verify(estimateRepository, times(1)).findById(anyLong());
        verify(ordersRepository, times(1)).findByEstimateId(anyLong());
        verify(ordersRepository, times(1)).save(any(Orders.class));
    }

    @Test
    @DisplayName("주문 등록 시 기존 주문 반환")
    void insertOrdersExistingOrder() {
        OrdersRequestDto requestDto = OrdersRequestDto.builder()
                .orderName("미용 견적서")
                .amount(10000)
                .tossOrderId("TOSS123")
                .build();

        given(estimateRepository.findById(anyLong())).willReturn(Optional.of(mockEstimate));
        given(ordersRepository.findByEstimateId(anyLong())).willReturn(Optional.of(mockOrder));

        OrdersResponseDto response = ordersService.insertOrders(1L, requestDto);

        assertThat(response).isNotNull();
        assertThat(response.getOrderName()).isEqualTo("미용 견적서");
        assertThat(response.getAmount()).isEqualTo(10000);
        assertThat(response.getTossOrderId()).isEqualTo("TOSS123");

        verify(estimateRepository, times(1)).findById(anyLong());
        verify(ordersRepository, times(1)).findByEstimateId(anyLong());
        verify(ordersRepository, times(0)).save(any(Orders.class));
    }

    @Test
    @DisplayName("주문 등록 시 견적서가 없는 경우 예외 발생")
    void insertOrdersEstimateNotFound() {
        OrdersRequestDto requestDto = OrdersRequestDto.builder()
                .orderName("미용 견적서")
                .amount(10000)
                .tossOrderId("TOSS123")
                .build();

        given(estimateRepository.findById(anyLong())).willReturn(Optional.empty());

        assertThatThrownBy(() -> ordersService.insertOrders(1L, requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("견적서를 찾을 수 없습니다");

        verify(estimateRepository, times(1)).findById(anyLong());
        verify(ordersRepository, times(0)).findByEstimateId(anyLong());
        verify(ordersRepository, times(0)).save(any(Orders.class));
    }
}
