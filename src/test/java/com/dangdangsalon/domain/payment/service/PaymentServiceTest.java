package com.dangdangsalon.domain.payment.service;

import com.dangdangsalon.domain.orders.entity.OrderStatus;
import com.dangdangsalon.domain.orders.entity.Orders;
import com.dangdangsalon.domain.orders.repository.OrdersRepository;
import com.dangdangsalon.domain.payment.dto.PaymentApproveRequestDto;
import com.dangdangsalon.domain.payment.dto.PaymentApproveResponseDto;
import com.dangdangsalon.domain.payment.dto.PaymentCancelRequestDto;
import com.dangdangsalon.domain.payment.dto.PaymentCancelResponseDto;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private OrdersRepository ordersRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

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
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private PaymentService paymentService;

    private Orders mockOrder;
    private User mockUser;
    private PaymentApproveRequestDto approveRequestDto;
    private PaymentCancelRequestDto cancelRequestDto;

    private static final String TEST_APPROVE_URL = "https://api.tosspayments.com/v1/payments/confirm";
    private static final String TEST_CANCEL_URL = "https://api.tosspayments.com/v1/payments/{paymentKey}/cancel";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "tossApiKey", "test-api-key");
        ReflectionTestUtils.setField(paymentService, "tossCancelUrl", "https://api.tosspayments.com/v1/payments/{paymentKey}/cancel");
        ReflectionTestUtils.setField(paymentService, "tossApproveUrl", "https://api.tosspayments.com/v1/payments/confirm");

        mockUser = User.builder()
                .name("이민수")
                .build();

        mockOrder = Orders.builder()
                .user(mockUser)
                .amountValue(10000)
                .status(OrderStatus.PENDING)
                .tossOrderId("TOSS_ORDER_123")
                .build();

        approveRequestDto = PaymentApproveRequestDto.builder()
                .paymentKey("PAYMENT_KEY_123")
                .orderId("TOSS_ORDER_123")
                .amount(10000)
                .build();

        cancelRequestDto = PaymentCancelRequestDto.builder()
                .paymentKey("PAYMENT_KEY_123")
                .cancelReason("생각해보니까 너무 비싼 거 같아요")
                .build();
    }

    @Test
    @DisplayName("결제 승인 - 주문 금액 불일치")
    void approvePayment_AmountMismatch() {
        // Given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(anyString(), anyString(), any())).willReturn(true);

        Orders mismatchOrder = Orders.builder()
                .user(mockUser)
                .amountValue(20000) // 다른 금액
                .status(OrderStatus.PENDING)
                .tossOrderId("TOSS_ORDER_123")
                .build();

        given(ordersRepository.findByTossOrderId(anyString())).willReturn(Optional.of(mismatchOrder));

        // When & Then
        assertThatThrownBy(() -> paymentService.approvePayment(approveRequestDto, "test-idempotency-key"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("결제 금액이 주문 금액과 일치하지 않습니다.");
    }

    @Test
    @DisplayName("결제 승인 - 중복 요청")
    void approvePayment_DuplicateRequest() {
        // Given
        String idempotencyKey = "test-idempotency-key";

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(anyString(),anyString(), any()))
                .willReturn(false);

        // When & Then
        assertThatThrownBy(() -> paymentService.approvePayment(approveRequestDto, idempotencyKey))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 동일한 결제 승인 요청이 처리 중입니다.");

        verify(redisTemplate.opsForValue()).setIfAbsent(anyString(),anyString(), any());
        verifyNoInteractions(ordersRepository);
    }

    @Test
    @DisplayName("결제 취소 - 중복 요청")
    void cancelPayment_DuplicateRequest() {
        // Given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(anyString(),anyString(), any()))
                .willReturn(false);

        // When & Then
        assertThatThrownBy(() -> paymentService.cancelPayment(cancelRequestDto, "test-idempotency-key"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 동일한 결제 취소 요청이 처리 중입니다.");
    }

    @Test
    @DisplayName("결제 취소 - 결제 정보 없음")
    void cancelPayment_PaymentNotFound() {
        // Given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(anyString(), anyString(), any())).willReturn(true);
        given(paymentRepository.findByPaymentKey(anyString())).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> paymentService.cancelPayment(cancelRequestDto, "test-idempotency-key"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("결제 정보를 찾을 수 없습니다");

        verify(paymentRepository).findByPaymentKey(anyString());
    }

    @Test
    @DisplayName("결제 승인 - 요청 성공")
    void approvePayment_Success() {
        // Given
        PaymentApproveResponseDto responseDto = PaymentApproveResponseDto.builder()
                .paymentKey("PAYMENT_KEY_123")
                .totalAmount(10000)
                .status("APPROVED")
                .approvedAt(OffsetDateTime.now())
                .method("CARD")
                .build();

        // webClient 설정
        given(webClient.post()).willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.uri(anyString())).willReturn(requestBodySpec);
        given(requestBodySpec.header(anyString(), anyString())).willReturn(requestBodySpec);
        given(requestBodySpec.bodyValue(any())).willAnswer(invocation -> requestHeadersSpec);
        given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.bodyToMono(PaymentApproveResponseDto.class)).willReturn(Mono.just(responseDto));

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(anyString(), anyString(), any())).willReturn(true);

        given(ordersRepository.findByTossOrderId(anyString())).willReturn(Optional.of(mockOrder));

        // When
        PaymentApproveResponseDto result = paymentService.approvePayment(approveRequestDto, "test-idempotency-key");

        // Then
        assertThat(result.getStatus()).isEqualTo("APPROVED");
        verify(ordersRepository).findByTossOrderId(anyString());
        verify(redisTemplate).delete(anyString());
    }

    @Test
    @DisplayName("결제 취소 - 요청 성공")
    void cancelPayment_Success() {
        // Given
        Payment mockPayment = Payment.builder()
                .paymentKey("PAYMENT_KEY_123")
                .totalAmount(10000)
                .paymentStatus(PaymentStatus.ACCEPTED)
                .build();

        PaymentCancelResponseDto responseDto = PaymentCancelResponseDto.builder()
                .paymentKey("PAYMENT_KEY_123")
                .orderId("ORDER_123")
                .status("CANCELED")
                .build();

        // webClient 설정
        given(webClient.post()).willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.uri(anyString())).willReturn(requestBodySpec);
        given(requestBodySpec.header(anyString(), anyString())).willReturn(requestBodySpec);
        given(requestBodySpec.bodyValue(any())).willAnswer(invocation -> requestHeadersSpec);
        given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.bodyToMono(PaymentCancelResponseDto.class)).willReturn(Mono.just(responseDto));

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(anyString(), anyString(), any())).willReturn(true);

        given(paymentRepository.findByPaymentKey(anyString())).willReturn(Optional.of(mockPayment));

        // When
        PaymentCancelResponseDto result = paymentService.cancelPayment(cancelRequestDto, "test-idempotency-key");

        // Then
        assertThat(result.getStatus()).isEqualTo("CANCELED");

        verify(paymentRepository).findByPaymentKey(anyString());
    }

    @Test
    @DisplayName("결제 승인 - Toss API 호출 오류")
    void approvePayment_TossApiError() {
        // Given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(anyString(), anyString(), any())).willReturn(true);
        given(ordersRepository.findByTossOrderId(anyString())).willReturn(Optional.of(mockOrder));

        // Toss API에서 오류 발생 시 예외 던지기
        given(webClient.post()).willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.uri(anyString())).willReturn(requestBodySpec);
        given(requestBodySpec.header(anyString(), anyString())).willReturn(requestBodySpec);
        given(requestBodySpec.bodyValue(any())).willAnswer(invocation -> requestHeadersSpec);
        given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.bodyToMono(PaymentApproveResponseDto.class))
                .willThrow(new WebClientResponseException(400, "Bad Request", null, null, null));

        // When & Then
        assertThatThrownBy(() -> paymentService.approvePayment(approveRequestDto, "test-idempotency-key"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("결제 승인 중 오류가 발생했습니다.");

        verify(ordersRepository).findByTossOrderId(anyString());
        verifyNoInteractions(paymentRepository);
    }

    @Test
    @DisplayName("결제 취소 - Toss API 호출 오류")
    void cancelPayment_TossApiError() {
        // Given
        Payment mockPayment = Payment.builder()
                .paymentKey("PAYMENT_KEY_123")
                .totalAmount(10000)
                .paymentStatus(PaymentStatus.ACCEPTED)
                .build();

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(anyString(), anyString(), any())).willReturn(true);
        given(paymentRepository.findByPaymentKey(anyString())).willReturn(Optional.of(mockPayment));

        // Toss API에서 오류 발생 시 예외 던지기
        given(webClient.post()).willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.uri(anyString())).willReturn(requestBodySpec);
        given(requestBodySpec.header(anyString(), anyString())).willReturn(requestBodySpec);
        given(requestBodySpec.bodyValue(any())).willAnswer(invocation -> requestHeadersSpec);
        given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.bodyToMono(PaymentCancelResponseDto.class))
                .willThrow(new WebClientResponseException(400, "Bad Request", null, null, null));

        // When & Then
        assertThatThrownBy(() -> paymentService.cancelPayment(cancelRequestDto, "test-idempotency-key"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("결제 취소 중 오류가 발생했습니다.");

        verify(paymentRepository).findByPaymentKey(anyString());
        verifyNoMoreInteractions(paymentRepository); // 결제가 저장되지 않았는지 확인
    }

    @Test
    @DisplayName("결제 금액 유효성 검사 0 이하 금액")
    void validatePaymentAmount_InvalidAmount_UsingReflection() throws Exception {
        // Given
        Method method = PaymentService.class.getDeclaredMethod("validatePaymentAmount", long.class);
        method.setAccessible(true); // private 메서드 접근 허용

        // When & Then
        Throwable thrown = catchThrowable(() -> method.invoke(paymentService, 0L));

        assertThat(thrown).isInstanceOf(InvocationTargetException.class);
        assertThat(thrown.getCause()) // 실제 예외 확인
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("결제 금액은 0보다 커야 합니다.");
    }

    @Test
    @DisplayName("결제 금액 유효성 검사 유효한 금액")
    void validatePaymentAmount_ValidAmount_UsingReflection() throws Exception {
        // Given
        Method method = PaymentService.class.getDeclaredMethod("validatePaymentAmount", long.class);
        method.setAccessible(true); // private 메서드 접근 허용

        // When & Then
        assertThatCode(() -> method.invoke(paymentService, 10000L))
                .doesNotThrowAnyException();
    }

}