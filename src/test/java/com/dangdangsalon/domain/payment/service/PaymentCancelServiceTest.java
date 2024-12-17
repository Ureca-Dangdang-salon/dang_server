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
import org.springframework.web.reactive.function.client.WebClientResponseException;
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
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private PaymentCancelRequestDto cancelRequestDto;
    private Payment mockPayment;

    private static final String TEST_CANCEL_URL = "https://api.tosspayments.com/v1/payments/{paymentKey}/cancel";

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(paymentCancelService, "tossApiKey", "test-api-key");
        ReflectionTestUtils.setField(paymentCancelService, "tossCancelUrl", TEST_CANCEL_URL);

        cancelRequestDto = PaymentCancelRequestDto.builder()
                .paymentKey("PAYMENT_KEY_123")
                .cancelReason("취소 이유")
                .build();

        mockPayment = Payment.builder()
                .paymentKey("PAYMENT_KEY_123")
                .paymentStatus(PaymentStatus.ACCEPTED)
                .build();
    }

    @Test
    @DisplayName("결제 취소 - 요청 성공")
    void cancelPayment_Success() {
        // Given
        EstimateRequest mockEstimateRequest = EstimateRequest.builder()
                .build();
        ReflectionTestUtils.setField(mockEstimateRequest, "id", 1L);

        Estimate mockEstimate = Estimate.builder()
                .estimateRequest(mockEstimateRequest)
                .status(EstimateStatus.ACCEPTED)
                .build();
        ReflectionTestUtils.setField(mockEstimate, "id", 1L);

        Orders mockOrders = Orders.builder()
                .estimate(mockEstimate)
                .build();
        ReflectionTestUtils.setField(mockOrders, "id", 1L); // 주문 ID 설정

        Payment mockPayment = Payment.builder()
                .paymentKey("PAYMENT_KEY_123")
                .paymentStatus(PaymentStatus.ACCEPTED)
                .orders(mockOrders)
                .build();

        PaymentCancelResponseDto responseDto = PaymentCancelResponseDto.builder()
                .paymentKey("PAYMENT_KEY_123")
                .status("CANCELED")
                .build();

        // 목 설정
        given(paymentRepository.findByPaymentKey(anyString())).willReturn(Optional.of(mockPayment));
        given(estimateRepository.findById(anyLong())).willReturn(Optional.of(mockEstimate));
        given(estimateRequestRepository.findById(anyLong())).willReturn(Optional.of(mockEstimateRequest));
        given(ordersRepository.findById(anyLong())).willReturn(Optional.of(mockOrders)); // 추가된 설정
        given(webClient.post()).willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.uri(anyString())).willReturn(requestBodySpec);
        given(requestBodySpec.header(anyString(), anyString())).willReturn(requestBodySpec);
        given(requestBodySpec.bodyValue(any())).willAnswer(invocation -> requestHeadersSpec);
        given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.bodyToMono(PaymentCancelResponseDto.class)).willReturn(Mono.just(responseDto));

        // When
        PaymentCancelResponseDto result = paymentCancelService.processPaymentCancellation(cancelRequestDto, "IDEMPOTENCY_KEY_123");

        // Then
        assertThat(result.getStatus()).isEqualTo("CANCELED");
        assertThat(result.getPaymentKey()).isEqualTo("PAYMENT_KEY_123");

        verify(paymentRepository).findByPaymentKey(anyString());
        verify(estimateRepository).findById(mockEstimate.getId());
        verify(estimateRequestRepository).findById(mockEstimateRequest.getId());
        verify(ordersRepository).findById(mockOrders.getId()); // 검증 추가
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

    @Test
    @DisplayName("결제 취소 - Toss API 호출 실패")
    void cancelPayment_TossApiFailure() {
        // Mock 설정
        given(paymentRepository.findByPaymentKey(anyString())).willReturn(Optional.of(mockPayment));
        given(webClient.post()).willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.uri(anyString())).willReturn(requestBodySpec);
        given(requestBodySpec.header(anyString(), anyString())).willReturn(requestBodySpec);
        given(requestBodySpec.bodyValue(any())).willAnswer(invocation -> requestHeadersSpec);
        given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.bodyToMono(PaymentCancelResponseDto.class))
                .willThrow(new RuntimeException("결제 취소 중 오류가 발생했습니다."));

        // 테스트 실행 및 검증
        assertThatThrownBy(() -> paymentCancelService.processPaymentCancellation(cancelRequestDto, "IDEMPOTENCY_KEY_123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("결제 취소 중 오류가 발생했습니다.");
    }

    @Test
    @DisplayName("결제 취소 - 재시도 실패로 REJECTED 상태 저장")
    void cancelPayment_RecoveryFailure() {
        // Given
        PaymentCancelRequestDto failedRequestDto = PaymentCancelRequestDto.builder()
                .paymentKey("FAILED_PAYMENT_KEY")
                .cancelReason("취소 실패 테스트")
                .build();

        Payment payment = Payment.builder()
                .paymentKey("FAILED_PAYMENT_KEY")
                .paymentStatus(PaymentStatus.ACCEPTED)
                .build();

        given(paymentRepository.findByPaymentKey("FAILED_PAYMENT_KEY")).willReturn(Optional.of(payment));

        WebClientResponseException exception = new WebClientResponseException(
                500, "Internal Server Error", null, null, null, null);

        // When
        paymentCancelService.recover(exception, failedRequestDto);

        // Then
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.REJECTED);

        verify(paymentRepository).findByPaymentKey("FAILED_PAYMENT_KEY");
    }

}