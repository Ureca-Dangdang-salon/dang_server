package com.dangdangsalon.domain.coupon.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueStatusDto {

    @JsonProperty("eventId")
    private Long eventId;

    @JsonProperty("queueLength")
    private Integer queueLength;

    @JsonProperty("remainingCoupons")
    private Integer remainingCoupons;

    @JsonProperty("aheadCount")
    private Integer aheadCount;

    @JsonProperty("behindCount")
    private Integer behindCount;

    @JsonProperty("estimatedTime")
    private Integer estimatedTime;
}
