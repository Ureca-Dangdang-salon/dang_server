package com.dangdangsalon.domain.payment.service;

import com.dangdangsalon.domain.coupon.entity.Coupon;
import com.dangdangsalon.domain.coupon.entity.CouponStatus;
import com.dangdangsalon.domain.coupon.repository.CouponRepository;
import com.dangdangsalon.domain.estimate.entity.Estimate;
import com.dangdangsalon.domain.estimate.entity.EstimateStatus;
import com.dangdangsalon.domain.estimate.repository.EstimateRepository;
import com.dangdangsalon.domain.estimate.request.entity.EstimateRequest;
import com.dangdangsalon.domain.estimate.request.entity.RequestStatus;
import com.dangdangsalon.domain.estimate.request.repository.EstimateRequestRepository;
import com.dangdangsalon.domain.orders.entity.OrderStatus;
import com.dangdangsalon.domain.orders.entity.Orders;
import com.dangdangsalon.domain.orders.repository.OrdersRepository;
import com.dangdangsalon.domain.payment.dto.PaymentCancelRequestDto;
import com.dangdangsalon.domain.payment.dto.PaymentCancelResponseDto;
import com.dangdangsalon.domain.payment.entity.Payment;
import com.dangdangsalon.domain.payment.entity.PaymentStatus;
import com.dangdangsalon.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
class PaymentCancelService {
    private final PaymentRepository paymentRepository;
    private final EstimateRepository estimateRepository;
    private final EstimateRequestRepository estimateRequestRepository;
    private final CouponRepository couponRepository;
    private final OrdersRepository ordersRepository;
    private final PaymentCancelRetryService paymentCancelRetryService;

    @Value("${toss.api.cancel-url}")
    private String tossCancelUrl;

    @Transactional
    public PaymentCancelResponseDto processPaymentCancellation(PaymentCancelRequestDto paymentCancelRequestDto, String idempotencyKey) {
        Payment payment = findPaymentByPaymentKey(paymentCancelRequestDto.getPaymentKey());

        String cancelUrl = tossCancelUrl.replace("{paymentKey}", paymentCancelRequestDto.getPaymentKey());

        PaymentCancelResponseDto paymentCancelResponseDto = paymentCancelRetryService.sendCancelRequestToToss(paymentCancelRequestDto, cancelUrl, idempotencyKey);

        Long couponId = paymentCancelRequestDto.getCouponId();

        if (couponId != null) {
            Coupon coupon = couponRepository.findById(couponId)
                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 쿠폰 ID 입니다: " + couponId));
            updateCouponStatusToNotUsed(coupon);
        }

        Orders orders = ordersRepository.findById(payment.getOrders().getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문 아이디입니다: " + payment.getOrders().getId()));

        orders.updateOrderStatus(OrderStatus.REJECTED);

        updatePaymentAndRelatedEntities(payment);

        return buildPaymentCancelResponseDto(paymentCancelResponseDto);
    }

    private Payment findPaymentByPaymentKey(String paymentKey) {
        return paymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));
    }

    private void updatePaymentAndRelatedEntities(Payment payment) {
        payment.updatePaymentStatus(PaymentStatus.CANCELED);

        Estimate estimate = findEstimateByPayment(payment);
        estimate.updateStatus(EstimateStatus.REFUND);

        EstimateRequest estimateRequest = findEstimateRequestByEstimate(estimate);
        estimateRequest.updateRequestStatus(RequestStatus.REFUND);
    }

    private Estimate findEstimateByPayment(Payment payment) {
        return estimateRepository.findById(payment.getOrders().getEstimate().getId())
                .orElseThrow(() -> new IllegalArgumentException("견적서를 찾을 수 없습니다"));
    }

    private EstimateRequest findEstimateRequestByEstimate(Estimate estimate) {
        return estimateRequestRepository.findById(estimate.getEstimateRequest().getId())
                .orElseThrow(() -> new IllegalArgumentException("견적 요청을 찾을 수 없습니다"));
    }

    private PaymentCancelResponseDto buildPaymentCancelResponseDto(PaymentCancelResponseDto responseDto) {
        return PaymentCancelResponseDto.builder()
                .paymentKey(responseDto.getPaymentKey())
                .orderId(responseDto.getOrderId())
                .status(responseDto.getStatus())
                .build();
    }

    private void updateCouponStatusToNotUsed(Coupon coupon) {
        coupon.updateCouponStatus(CouponStatus.NOT_USED);
    }
}