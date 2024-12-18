package com.dangdangsalon.domain.payment.service;

import com.dangdangsalon.domain.payment.dto.PaymentApproveRequestDto;
import com.dangdangsalon.domain.payment.dto.PaymentApproveResponseDto;
import com.dangdangsalon.domain.payment.entity.Payment;
import com.dangdangsalon.domain.payment.entity.PaymentStatus;
import com.dangdangsalon.domain.payment.repository.PaymentRepository;
import com.dangdangsalon.domain.orders.entity.Orders;
import com.dangdangsalon.domain.orders.repository.OrdersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentApproveRetryServiceTest {

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

    @Mock
    private OrdersRepository ordersRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentApproveRetryService paymentApproveRetryService;

    private PaymentApproveRequestDto requestDto;
    private Orders mockOrder;

    private static final String TEST_APPROVE_URL = "https://api.tosspayments.com/v1/payments/PAYMENT_KEY_123/approve";

    @BeforeEach
    void setUp() {
        requestDto = PaymentApproveRequestDto.builder()
                .paymentKey("PAYMENT_KEY_123")
                .orderId("ORDER_ID_123")
                .amount(50000)
                .build();

        mockOrder = Orders.builder()
                .tossOrderId("ORDER_ID_123")
                .build();
    }

    @Test
    @DisplayName("결제 승인 API 호출 성공")
    void sendApprovalRequestToToss_Success() {
        // Given
        PaymentApproveResponseDto responseDto = PaymentApproveResponseDto.builder()
                .paymentKey("PAYMENT_KEY_123")
                .status("DONE")
                .build();

        // WebClient Mock 설정
        given(webClient.post()).willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.uri(anyString())).willReturn(requestBodySpec);
        given(requestBodySpec.header(anyString(), anyString())).willReturn(requestBodySpec);
        given(requestBodySpec.bodyValue(any())).willAnswer(invocation -> requestHeadersSpec);
        given(requestHeadersSpec.retrieve()).willReturn(responseSpec);

        // 응답 모킹
        given(responseSpec.bodyToMono(PaymentApproveResponseDto.class))
                .willReturn(Mono.just(responseDto));

        // When
        PaymentApproveResponseDto result = paymentApproveRetryService.sendApprovalRequestToToss(requestDto, TEST_APPROVE_URL, "IDEMPOTENCY_KEY_123");

        // Then
        assertThat(result.getStatus()).isEqualTo("DONE");
        assertThat(result.getPaymentKey()).isEqualTo("PAYMENT_KEY_123");
    }

    @Test
    @DisplayName("결제 승인 API 재시도 실패 후 복구 로직 검증")
    void sendApprovalRequestToToss_Failure_And_Recovery() {
        // Given
        WebClientResponseException exception = WebClientResponseException.create(500, "Server Error", null, null, null);

        // 주문 Repository 설정
        given(ordersRepository.findByTossOrderId(anyString())).willReturn(Optional.of(mockOrder));

        Payment[] capturedPayment = new Payment[1];
        doAnswer(invocation -> {
            capturedPayment[0] = invocation.getArgument(0);
            return null;
        }).when(paymentRepository).save(any(Payment.class));

        // When
        PaymentApproveResponseDto result = paymentApproveRetryService.recover(exception, requestDto);

        // Then
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.REJECTED.name());

        // 저장된 결제 정보 검증
        verify(paymentRepository, times(1)).save(any(Payment.class));
        assertThat(capturedPayment[0]).isNotNull();
        assertThat(capturedPayment[0].getPaymentStatus()).isEqualTo(PaymentStatus.REJECTED);
        assertThat(capturedPayment[0].getPaymentKey()).isEqualTo("PAYMENT_KEY_123");
    }
}