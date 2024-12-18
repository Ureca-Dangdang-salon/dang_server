package com.dangdangsalon.domain.payment.service;

import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.entity.EstimateStatus;
import com.dangdangsalon.domain.estimate.repository.EstimateRepository;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestRepository;
import com.dangdangsalon.domain.orders.entity.Orders;
import com.dangdangsalon.domain.orders.repository.OrdersRepository;
import com.dangdangsalon.domain.payment.dto.PaymentCancelRequestDto;
import com.dangdangsalon.domain.payment.dto.PaymentCancelResponseDto;
import com.dangdangsalon.domain.payment.entity.Payment;
import com.dangdangsalon.domain.payment.entity.PaymentStatus;
import com.dangdangsalon.domain.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentCancelServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private EstimateRepository estimateRepository;

    @Mock
    private OrdersRepository ordersRepository;

    @Mock
    private EstimateRequestRepository estimateRequestRepository;

    @InjectMocks
    private PaymentCancelService paymentCancelService;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private PaymentCancelRetryService paymentCancelRetryService;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private PaymentCancelRequestDto cancelRequestDto;
    private Payment mockPayment;

    private static final String TEST_CANCEL_URL = "https://api.tosspayments.com/v1/payments/{paymentKey}/cancel";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentCancelService, "tossCancelUrl", TEST_CANCEL_URL);

        ReflectionTestUtils.setField(paymentCancelService, "paymentCancelRetryService", paymentCancelRetryService);

        cancelRequestDto = PaymentCancelRequestDto.builder()
                .paymentKey("PAYMENT_KEY_123")
                .cancelReason("취소 이유")
                .build();

        EstimateRequest mockEstimateRequest = EstimateRequest.builder().build();
        ReflectionTestUtils.setField(mockEstimateRequest, "id", 1L);

        Estimate mockEstimate = Estimate.builder()
                .estimateRequest(mockEstimateRequest)
                .status(EstimateStatus.ACCEPTED)
                .build();
        ReflectionTestUtils.setField(mockEstimate, "id", 1L);

        Orders mockOrders = Orders.builder()
                .estimate(mockEstimate)
                .build();
        ReflectionTestUtils.setField(mockOrders, "id", 1L);

        mockPayment = Payment.builder()
                .paymentKey("PAYMENT_KEY_123")
                .paymentStatus(PaymentStatus.ACCEPTED)
                .orders(mockOrders) // Orders 설정
                .build();
    }

    @Test
    @DisplayName("결제 취소 - 요청 성공")
    void cancelPayment_Success() {
        // Given
        EstimateRequest mockEstimateRequest = EstimateRequest.builder().build();
        ReflectionTestUtils.setField(mockEstimateRequest, "id", 1L);

        Estimate mockEstimate = Estimate.builder()
                .estimateRequest(mockEstimateRequest)
                .status(EstimateStatus.ACCEPTED)
                .build();
        ReflectionTestUtils.setField(mockEstimate, "id", 1L);

        Orders mockOrders = Orders.builder().estimate(mockEstimate).build();
        ReflectionTestUtils.setField(mockOrders, "id", 1L);

        PaymentCancelResponseDto responseDto = PaymentCancelResponseDto.builder()
                .paymentKey("PAYMENT_KEY_123")
                .status("CANCELED")
                .build();

        // Mock 설정
        given(paymentRepository.findByPaymentKey(anyString())).willReturn(Optional.of(mockPayment));
        given(estimateRepository.findById(anyLong())).willReturn(Optional.of(mockEstimate));
        given(estimateRequestRepository.findById(anyLong())).willReturn(Optional.of(mockEstimateRequest));
        given(ordersRepository.findById(anyLong())).willReturn(Optional.of(mockOrders));

        // paymentCancelRetryService 설정
        given(paymentCancelRetryService.sendCancelRequestToToss(any(), anyString(), anyString()))
                .willReturn(responseDto);

        // When
        PaymentCancelResponseDto result = paymentCancelService.processPaymentCancellation(cancelRequestDto, "IDEMPOTENCY_KEY_123");

        // Then
        assertThat(result.getStatus()).isEqualTo("CANCELED");
        assertThat(result.getPaymentKey()).isEqualTo("PAYMENT_KEY_123");

        verify(paymentRepository).findByPaymentKey(anyString());
        verify(paymentCancelRetryService).sendCancelRequestToToss(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("결제 취소 - 결제 정보 없음")
    void cancelPayment_NotFound() {
        // Mock 설정
        given(paymentRepository.findByPaymentKey(anyString())).willReturn(Optional.empty());

        // 테스트 실행 및 검증
        assertThatThrownBy(() -> paymentCancelService.processPaymentCancellation(cancelRequestDto, "IDEMPOTENCY_KEY_123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("결제 정보를 찾을 수 없습니다.");
    }

}