package com.dangdangsalon.domain.coupon.dto;

import com.dangdangsalon.domain.coupon.entity.CouponEvent;
import com.dangdangsalon.domain.coupon.entity.CouponEventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class CouponMainResponseDto {
    private Long eventId;
    private String eventName;
    private CouponEventStatus couponEventStatus;
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    public static CouponMainResponseDto create(CouponEvent couponEvent) {
        return CouponMainResponseDto.builder()
                .eventId(couponEvent.getId())
                .eventName(couponEvent.getName())
                .couponEventStatus(couponEvent.getStatus())
                .startAt(couponEvent.getStartedAt())
                .endAt(couponEvent.getEndedAt())
                .build();
    }

}
