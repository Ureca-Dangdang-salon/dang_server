package com.dangdangsalon.domain.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueStatusDto {

    private Long eventId;
    private Long queueLength;
    private Integer remainingCoupons;
}
