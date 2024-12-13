package com.dangdangsalon.domain.coupon.dto;

import com.dangdangsalon.domain.coupon.entity.Coupon;
import com.dangdangsalon.domain.coupon.entity.CouponStatus;
import com.dangdangsalon.domain.coupon.entity.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class CouponUserResponseDto {
    private Long couponId;
    private String name;
    private int discountAmount;
    private DiscountType discountType;
    private CouponStatus couponStatus;
    private LocalDateTime expiredAt;

    public static CouponUserResponseDto create(Coupon coupon) {
        return CouponUserResponseDto.builder()
                .couponId(coupon.getId())
                .name(coupon.getCouponName())
                .discountAmount(coupon.getDiscountAmount())
                .discountType(coupon.getDiscountType())
                .couponStatus(coupon.getStatus())
                .expiredAt(coupon.getExpiredAt())
                .build();
    }
}
