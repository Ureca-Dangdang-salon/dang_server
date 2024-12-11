package com.dangdangsalon.domain.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class QueueStatusResponse {
    private String eventName;
    private long queueSize;
    private long currentPosition;
    private long remainingCountAhead;
    private long remainingCountBehind;
    private String estimatedTime;
    private int progressPercentage;
    private boolean isIssued;
    public static QueueStatusResponse ofEmptyQueue(String eventName) {
        return new QueueStatusResponse(
                eventName,
                9999, // Queue size
                9999, // Rank
                9999, // Remaining ahead
                9999, // Remaining behind
                "99:99", // Estimated time
                9999, // Progress percentage
                false // Is queue empty
        );
    }
}
