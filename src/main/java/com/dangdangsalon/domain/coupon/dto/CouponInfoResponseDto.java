package com.dangdangsalon.domain.coupon.dto;

import com.dangdangsalon.domain.coupon.entity.Coupon;
import com.dangdangsalon.domain.coupon.entity.CouponEvent;
import com.dangdangsalon.domain.coupon.entity.CouponEventStatus;
import com.dangdangsalon.domain.coupon.entity.DiscountType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class CouponInfoResponseDto {
    private Long eventId;
    private String name;
    private int discountAmount;
    private DiscountType discountType;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private int totalQuantity;
    private int remainQuantity;
    private CouponEventStatus couponEventStatus;

    public static CouponInfoResponseDto create(CouponEvent couponEvent) {
        return CouponInfoResponseDto.builder()
                .eventId(couponEvent.getId())
                .name(couponEvent.getName())
                .discountAmount(couponEvent.getDiscountAmount())
                .discountType(couponEvent.getDiscountType())
                .startedAt(couponEvent.getStartedAt())
                .endedAt(couponEvent.getEndedAt())
                .totalQuantity(couponEvent.getTotalQuantity())
                .remainQuantity(couponEvent.getRemainQuantity())
                .couponEventStatus(couponEvent.getStatus())
                .build();

    }
}
