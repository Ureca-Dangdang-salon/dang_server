package com.dangdangsalon.domain.payment.service;

import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.repository.EstimateRepository;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestRepository;
import com.dangdangsalon.domain.orders.entity.Orders;
import com.dangdangsalon.domain.orders.repository.OrdersRepository;
import com.dangdangsalon.domain.payment.dto.PaymentApproveRequestDto;
import com.dangdangsalon.domain.payment.dto.PaymentApproveResponseDto;
import com.dangdangsalon.domain.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentApproveServiceTest {

    @Mock
    private OrdersRepository ordersRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentNotificationService paymentNotificationService;

    @Mock
    private EstimateRepository estimateRepository;

    @Mock
    private EstimateRequestRepository estimateRequestRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @InjectMocks
    private PaymentApproveService paymentApproveService;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private PaymentApproveRequestDto approveRequestDto;
    private Orders mockOrder;

    private static final String TEST_APPROVE_URL = "https://api.tosspayments.com/v1/payments/confirm";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentApproveService, "tossApiKey", "test-api-key");
        ReflectionTestUtils.setField(paymentApproveService, "tossApproveUrl", TEST_APPROVE_URL);

        approveRequestDto = PaymentApproveRequestDto.builder()
                .paymentKey("PAYMENT_KEY_123")
                .orderId("TOSS_ORDER_123")
                .amount(10000)
                .build();

        EstimateRequest estimateRequest = EstimateRequest.builder()
                .build();

        ReflectionTestUtils.setField(estimateRequest, "id", 1L);

        Estimate estimate = Estimate.builder()
                .estimateRequest(estimateRequest)
                .build();
        ReflectionTestUtils.setField(estimate, "id", 1L);

        mockOrder = Orders.builder()
                .amountValue(10000)
                .tossOrderId("TOSS_ORDER_123")
                .estimate(estimate)
                .build();
    }

    @Test
    @DisplayName("결제 승인 - 요청 성공")
    void approvePayment_Success() {
        PaymentApproveResponseDto responseDto = PaymentApproveResponseDto.builder()
                .paymentKey("PAYMENT_KEY_123")
                .totalAmount(10000)
                .status("APPROVED")
                .approvedAt(OffsetDateTime.now())
                .method("CARD")
                .build();

        given(webClient.post()).willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.uri(anyString())).willReturn(requestBodySpec);
        given(requestBodySpec.header(anyString(), anyString())).willReturn(requestBodySpec);
        given(requestBodySpec.bodyValue(any())).willAnswer(invocation -> requestHeadersSpec);
        given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.bodyToMono(PaymentApproveResponseDto.class)).willReturn(Mono.just(responseDto));

        given(ordersRepository.findByTossOrderId(anyString())).willReturn(Optional.of(mockOrder));
        given(estimateRepository.findById(anyLong())).willReturn(Optional.of(mockOrder.getEstimate()));
        given(estimateRequestRepository.findById(anyLong())).willReturn(Optional.of(mockOrder.getEstimate().getEstimateRequest()));

        PaymentApproveResponseDto result = paymentApproveService.processPaymentApproval(approveRequestDto, "IDEMPOTENCY_KEY_123");

        assertThat(result.getStatus()).isEqualTo("APPROVED");
        assertThat(result.getPaymentKey()).isEqualTo("PAYMENT_KEY_123");
        verify(ordersRepository).findByTossOrderId(anyString());
        verify(estimateRepository).findById(anyLong());
        verify(estimateRequestRepository).findById(anyLong());
    }

    @Test
    @DisplayName("결제 승인 - 결제 금액 불일치")
    void approvePayment_AmountMismatch() {
        Orders orderWithDifferentAmount = Orders.builder()
                .amountValue(5000)
                .tossOrderId("TOSS_ORDER_123")
                .build();

        given(ordersRepository.findByTossOrderId(anyString())).willReturn(Optional.of(orderWithDifferentAmount));

        assertThatThrownBy(() -> paymentApproveService.processPaymentApproval(approveRequestDto, "IDEMPOTENCY_KEY_123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("결제 금액이 주문 금액과 일치하지 않습니다.");
    }

    @Test
    @DisplayName("결제 승인 - 주문 정보 없음")
    void approvePayment_OrderNotFound() {
        given(ordersRepository.findByTossOrderId(anyString())).willReturn(Optional.empty());

        assertThatThrownBy(() -> paymentApproveService.processPaymentApproval(approveRequestDto, "IDEMPOTENCY_KEY_123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("주문 정보를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("결제 승인 - Toss API 호출 실패")
    void approvePayment_TossApiFailure() {
        given(ordersRepository.findByTossOrderId(anyString())).willReturn(Optional.of(mockOrder));

        given(webClient.post()).willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.uri(anyString())).willReturn(requestBodySpec);
        given(requestBodySpec.header(anyString(), anyString())).willReturn(requestBodySpec);
        given(requestBodySpec.bodyValue(any())).willAnswer(invocation -> requestHeadersSpec);
        given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.bodyToMono(PaymentApproveResponseDto.class))
                .willThrow(new RuntimeException("결제 승인 중 오류가 발생했습니다."));

        assertThatThrownBy(() -> paymentApproveService.processPaymentApproval(approveRequestDto, "IDEMPOTENCY_KEY_123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("결제 승인 중 오류가 발생했습니다.");
    }
}
