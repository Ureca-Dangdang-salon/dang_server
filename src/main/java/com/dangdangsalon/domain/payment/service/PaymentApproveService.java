package com.dangdangsalon.domain.payment.service;

import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.entity.EstimateStatus;
import com.dangdangsalon.domain.estimate.repository.EstimateRepository;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.estimate.request.entity.RequestStatus;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestRepository;
import com.dangdangsalon.domain.orders.entity.OrderStatus;
import com.dangdangsalon.domain.orders.entity.Orders;
import com.dangdangsalon.domain.orders.repository.OrdersRepository;
import com.dangdangsalon.domain.payment.dto.PaymentApproveRequestDto;
import com.dangdangsalon.domain.payment.dto.PaymentApproveResponseDto;
import com.dangdangsalon.domain.payment.entity.Payment;
import com.dangdangsalon.domain.payment.entity.PaymentStatus;
import com.dangdangsalon.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
class PaymentApproveService {
    private final OrdersRepository ordersRepository;
    private final PaymentRepository paymentRepository;
    private final EstimateRepository estimateRepository;
    private final EstimateRequestRepository estimateRequestRepository;
    private final PaymentNotificationService paymentNotificationService;
    private final WebClient webClient;

    @Value("${toss.api.key}")
    private String tossApiKey;

    @Value("${toss.api.approve-url}")
    private String tossApproveUrl;

    @Transactional
    public PaymentApproveResponseDto processPaymentApproval(PaymentApproveRequestDto paymentApproveRequestDto, String idempotencyKey) {
        validatePaymentAmount(paymentApproveRequestDto.getAmount());

        Orders order = findOrderByTossOrderId(paymentApproveRequestDto.getOrderId());
        validateOrderAmount(order, paymentApproveRequestDto.getAmount());

        PaymentApproveResponseDto paymentResponse = sendApprovalRequestToToss(
                paymentApproveRequestDto,
                tossApproveUrl,
                idempotencyKey
        );

        Payment payment = createPayment(order, paymentResponse);
        updateOrderStatus(order);
        updateEstimateAndRequestStatus(order);

        paymentNotificationService.sendNotificationToUser(order);

        return paymentResponse;
    }

    @Retryable(
            retryFor = { WebClientResponseException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public PaymentApproveResponseDto sendApprovalRequestToToss(PaymentApproveRequestDto paymentApproveRequestDto, String url, String idempotencyKey) {
        String authorizationHeader = "Basic " + Base64.getEncoder().encodeToString((tossApiKey + ":").getBytes());

        try {
            return webClient.post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                    .header("Idempotency-Key", idempotencyKey)
                    .bodyValue(Map.of(
                            "paymentKey", paymentApproveRequestDto.getPaymentKey(),
                            "orderId", paymentApproveRequestDto.getOrderId(),
                            "amount", paymentApproveRequestDto.getAmount()
                    ))
                    .retrieve()
                    .bodyToMono(PaymentApproveResponseDto.class)
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("Toss 결제 검증 API 호출 실패: {}", ex.getResponseBodyAsString());
            throw new RuntimeException("결제 승인 중 오류가 발생했습니다.");
        }
    }

    @Recover
    public void recover(WebClientResponseException ex, PaymentApproveRequestDto requestDto) {
        log.error("결제 승인 재시도 실패: 요청 ID={}, 오류={}", requestDto.getOrderId(), ex.getMessage());
    }

    private void validatePaymentAmount(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("결제 금액은 0보다 커야 합니다.");
        }
    }

    private Orders findOrderByTossOrderId(String tossOrderId) {
        return ordersRepository.findByTossOrderId(tossOrderId)
                .orElseThrow(() -> new IllegalArgumentException("주문 정보를 찾을 수 없습니다: " + tossOrderId));
    }

    private void validateOrderAmount(Orders order, long paymentAmount) {
        if (order.getAmountValue() != paymentAmount) {
            throw new IllegalArgumentException("결제 금액이 주문 금액과 일치하지 않습니다.");
        }
    }

    private Payment createPayment(Orders order, PaymentApproveResponseDto paymentResponse) {
        Payment payment = Payment.builder()
                .paymentKey(paymentResponse.getPaymentKey())
                .totalAmount(paymentResponse.getTotalAmount())
                .paymentStatus(PaymentStatus.ACCEPTED)
                .requestedAt(paymentResponse.getApprovedAt().toLocalDateTime())
                .paymentMethod(paymentResponse.getMethod())
                .orders(order)
                .build();

        return paymentRepository.save(payment);
    }

    private void updateOrderStatus(Orders order) {
        order.updateOrderStatus(OrderStatus.ACCEPTED);
    }

    private void updateEstimateAndRequestStatus(Orders order) {
        Estimate estimate = findEstimateById(order.getEstimate().getId());
        estimate.updateStatus(EstimateStatus.PAID);

        EstimateRequest estimateRequest = findEstimateRequestById(estimate.getEstimateRequest().getId());
        estimateRequest.updateRequestStatus(RequestStatus.PAID);
    }

    private Estimate findEstimateById(Long estimateId) {
        return estimateRepository.findById(estimateId)
                .orElseThrow(() -> new IllegalArgumentException("견적서를 찾을 수 없습니다: " + estimateId));
    }

    private EstimateRequest findEstimateRequestById(Long requestId) {
        return estimateRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("견적 요청을 찾을 수 없습니다: " + requestId));
    }
}