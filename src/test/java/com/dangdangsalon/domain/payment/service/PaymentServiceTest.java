package com.dangdangsalon.domain.payment.service;

import com.dangdangsalon.domain.payment.dto.PaymentApproveRequestDto;
import com.dangdangsalon.domain.payment.dto.PaymentApproveResponseDto;
import com.dangdangsalon.domain.payment.dto.PaymentCancelRequestDto;
import com.dangdangsalon.domain.payment.dto.PaymentCancelResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentApproveService paymentApproveService;

    @Mock
    private PaymentCancelService paymentCancelService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentApproveRequestDto approveRequestDto;
    private PaymentCancelRequestDto cancelRequestDto;

    @BeforeEach
    void setUp() {
        approveRequestDto = PaymentApproveRequestDto.builder()
                .paymentKey("PAYMENT_KEY_123")
                .orderId("ORDER_123")
                .amount(10000)
                .build();

        cancelRequestDto = PaymentCancelRequestDto.builder()
                .paymentKey("PAYMENT_KEY_123")
                .cancelReason("취소 사유")
                .build();
    }

    @Test
    @DisplayName("결제 승인 - 요청 성공")
    void approvePayment_Success() {
        // Given
        PaymentApproveResponseDto responseDto = PaymentApproveResponseDto.builder()
                .paymentKey("PAYMENT_KEY_123")
                .status("APPROVED")
                .build();

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).willReturn(true);
        given(paymentApproveService.processPaymentApproval(any(), anyString())).willReturn(responseDto);

        // When
        PaymentApproveResponseDto result = paymentService.approvePayment(approveRequestDto, 1L);

        // Then
        assertThat(result.getStatus()).isEqualTo("APPROVED");
        verify(valueOperations).setIfAbsent(anyString(), anyString(), any(Duration.class));
        verify(paymentApproveService).processPaymentApproval(any(), anyString());
        verify(redisTemplate).delete(anyString());
    }

    @Test
    @DisplayName("결제 승인 - 중복 요청")
    void approvePayment_DuplicateRequest() {
        // Given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> paymentService.approvePayment(approveRequestDto, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 동일한 결제 승인 요청이 처리 중입니다.");
    }

    @Test
    @DisplayName("결제 취소 - 요청 성공")
    void cancelPayment_Success() {
        // Given
        PaymentCancelResponseDto responseDto = PaymentCancelResponseDto.builder()
                .paymentKey("PAYMENT_KEY_123")
                .status("CANCELED")
                .build();

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).willReturn(true);
        given(paymentCancelService.processPaymentCancellation(any(), anyString())).willReturn(responseDto);

        // When
        PaymentCancelResponseDto result = paymentService.cancelPayment(cancelRequestDto, 1L);

        // Then
        assertThat(result.getStatus()).isEqualTo("CANCELED");
        verify(valueOperations).setIfAbsent(anyString(), anyString(), any(Duration.class));
        verify(paymentCancelService).processPaymentCancellation(any(), anyString());
        verify(redisTemplate).delete(anyString());
    }

    @Test
    @DisplayName("결제 취소 - 중복 요청")
    void cancelPayment_DuplicateRequest() {
        // Given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> paymentService.cancelPayment(cancelRequestDto, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 동일한 결제 취소 요청이 처리 중입니다.");
    }
}
