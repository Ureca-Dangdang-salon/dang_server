package com.dangdangsalon.domain.payment.dto;

import com.dangdangsalon.domain.coupon.entity.Coupon;
import com.dangdangsalon.domain.coupon.entity.DiscountType;
import com.dangdangsalon.domain.estimate.entity.EstimateStatus;
import com.dangdangsalon.domain.orders.entity.Orders;
import com.dangdangsalon.domain.payment.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {

    private String groomerName;
    private String groomerImage;
    private LocalDateTime paymentDate;
    private LocalDateTime reservationDate;
    private List<PaymentDogProfileResponseDto> dogProfileList;
    private int totalAmount;
    private String status;
    private EstimateStatus estimateStatus;
    private String paymentKey;
    private Long couponId;
    private String couponName;
    private int discountAmount;
    private DiscountType discountType;
    private LocalDateTime expiredAt;

    public static PaymentResponseDto from(Payment payment, Orders order, Coupon coupon, List<PaymentDogProfileResponseDto> dogProfileList) {
        return PaymentResponseDto.builder()
                .groomerName(order.getEstimate().getGroomerProfile().getName())
                .groomerImage(order.getEstimate().getGroomerProfile().getImageKey())
                .reservationDate(order.getEstimate().getDate())
                .paymentDate(payment.getRequestedAt())
                .dogProfileList(dogProfileList)
                .totalAmount(payment.getTotalAmount())
                .status(payment.getPaymentStatus().toString())
                .estimateStatus(order.getEstimate().getStatus())
                .paymentKey(payment.getPaymentKey())
                .couponId(coupon != null ? coupon.getId() : null)
                .couponName(coupon != null ? coupon.getCouponName() : null)
                .discountType(coupon != null ? coupon.getDiscountType() : null)
                .discountAmount(coupon != null ? coupon.getDiscountAmount() : 0)
                .expiredAt(coupon != null ? coupon.getExpiredAt() : null)
                .build();
    }
}
