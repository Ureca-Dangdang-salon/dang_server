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
import com.dangdangsalon.domain.payment.dto.PaymentApproveRequestDto;
import com.dangdangsalon.domain.payment.dto.PaymentApproveResponseDto;
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
class PaymentApproveService {
    private final OrdersRepository ordersRepository;
    private final PaymentRepository paymentRepository;
    private final EstimateRepository estimateRepository;
    private final EstimateRequestRepository estimateRequestRepository;
    private final PaymentNotificationService paymentNotificationService;
    private final CouponRepository couponRepository;
    private final PaymentApproveRetryService retryService;

    @Value("${toss.api.approve-url}")
    private String tossApproveUrl;

    @Transactional
    public PaymentApproveResponseDto processPaymentApproval(PaymentApproveRequestDto paymentApproveRequestDto, String idempotencyKey) {
        validatePaymentAmount(paymentApproveRequestDto.getAmount());

        Orders order = findOrderByTossOrderId(paymentApproveRequestDto.getOrderId());
        validateOrderAmount(order, paymentApproveRequestDto.getAmount());

        PaymentApproveResponseDto paymentResponse = retryService.sendApprovalRequestToToss(
                paymentApproveRequestDto, tossApproveUrl, idempotencyKey);

        Long couponId = paymentApproveRequestDto.getCouponId();
        Coupon coupon = null;

        if (couponId != null) {
            coupon = couponRepository.findById(couponId)
                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 쿠폰 ID 입니다: " + couponId));
            updateCouponStatusToUsed(coupon);
        }

        Payment payment = createPayment(order, coupon, paymentResponse);
        updateOrderStatus(order);
        updateEstimateAndRequestStatus(order);

        paymentNotificationService.sendNotificationToUser(order);

        return paymentResponse;
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

    private Payment createPayment(Orders order, Coupon coupon, PaymentApproveResponseDto paymentResponse) {
        Payment payment = Payment.builder()
                .paymentKey(paymentResponse.getPaymentKey())
                .totalAmount(paymentResponse.getTotalAmount())
                .paymentStatus(PaymentStatus.ACCEPTED)
                .requestedAt(paymentResponse.getApprovedAt().toLocalDateTime())
                .paymentMethod(paymentResponse.getMethod())
                .orders(order)
                .coupon(coupon)
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

    private void updateCouponStatusToUsed(Coupon coupon) {

        if (coupon.getStatus() != CouponStatus.NOT_USED) {
            throw new IllegalStateException("쿠폰을 사용할 수 없는 상태입니다.");
        }
        coupon.updateCouponStatus(CouponStatus.USED);
    }
}