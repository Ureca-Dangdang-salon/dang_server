package com.dangdangsalon.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FcmTokenTopicRequestDto {
    private String fcmToken;
    private String topic;
}
