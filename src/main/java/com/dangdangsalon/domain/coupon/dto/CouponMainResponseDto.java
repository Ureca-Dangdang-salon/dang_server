package com.dangdangsalon.domain.coupon.dto;

import com.dangdangsalon.domain.coupon.entity.CouponEvent;
import com.dangdangsalon.domain.coupon.entity.CouponEventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CouponMainResponseDto {
    private Long eventId;
    private String eventName;
    private CouponEventStatus couponEventStatus;

    public static CouponMainResponseDto create(CouponEvent couponEvent) {
        return CouponMainResponseDto.builder()
                .eventId(couponEvent.getId())
                .eventName(couponEvent.getName())
                .couponEventStatus(couponEvent.getStatus())
                .build();
    }

}
