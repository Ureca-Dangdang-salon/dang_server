package com.dangdangsalon.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewNotificationDto {
    private Long userId;
    private Long estimateId;
    private String scheduledTime;
}
