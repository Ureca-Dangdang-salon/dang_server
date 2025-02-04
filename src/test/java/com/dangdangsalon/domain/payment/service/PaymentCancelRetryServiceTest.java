package com.dangdangsalon.domain.payment.service;

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
class PaymentCancelRetryServiceTest {

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
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentCancelRetryService paymentCancelRetryService;

    private PaymentCancelRequestDto requestDto;
    private Payment mockPayment;

    private static final String TEST_CANCEL_URL = "https://api.tosspayments.com/v1/payments/PAYMENT_KEY_123/cancel";

    @BeforeEach
    void setUp() {
        requestDto = PaymentCancelRequestDto.builder()
                .paymentKey("PAYMENT_KEY_123")
                .cancelReason("Test Cancel")
                .build();

        mockPayment = Payment.builder()
                .paymentKey("PAYMENT_KEY_123")
                .paymentStatus(PaymentStatus.ACCEPTED)
                .build();
    }

    @Test
    @DisplayName("결제 취소 API 호출 성공")
    void sendCancelRequestToToss_Success() {
        // Given
        PaymentCancelResponseDto responseDto = PaymentCancelResponseDto.builder()
                .paymentKey("PAYMENT_KEY_123")
                .status("CANCELED")
                .build();

        // WebClient Mock 설정
        given(webClient.post()).willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.uri(anyString())).willReturn(requestBodySpec);
        given(requestBodySpec.header(anyString(), anyString())).willReturn(requestBodySpec);
        given(requestBodySpec.bodyValue(any())).willAnswer(invocation -> requestHeadersSpec);
        given(requestHeadersSpec.retrieve()).willReturn(responseSpec);

        // Use Mono.just() to mock the response
        given(responseSpec.bodyToMono(PaymentCancelResponseDto.class))
                .willReturn(Mono.just(responseDto));

        // When
        PaymentCancelResponseDto result = paymentCancelRetryService.sendCancelRequestToToss(requestDto, TEST_CANCEL_URL, "IDEMPOTENCY_KEY_123");

        // Then
        assertThat(result.getStatus()).isEqualTo("CANCELED");
        assertThat(result.getPaymentKey()).isEqualTo("PAYMENT_KEY_123");
    }

    @Test
    @DisplayName("결제 취소 API 재시도 실패 후 복구 로직 검증")
    void sendCancelRequestToToss_Failure_And_Recovery() {
        // Given
        WebClientResponseException exception = WebClientResponseException.create(500, "Server Error", null, null, null);

        // 결제 Repository 설정
        given(paymentRepository.findByPaymentKey(anyString())).willReturn(Optional.of(mockPayment));

        // When
        PaymentCancelResponseDto result = paymentCancelRetryService.recover(exception, requestDto);

        // Then
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.REJECTED.name());

        // 결제 정보 조회 메서드 호출 확인
        verify(paymentRepository, times(1)).findByPaymentKey("PAYMENT_KEY_123");
    }
}