package com.dangdangsalon.domain.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private String id;
    private String title;
    private String body;

    @JsonProperty("read")
    private boolean isRead;
    private LocalDateTime createdAt;
    private String type;
    private Long referenceId;

    public void updateIsRead(boolean isRead) {
        this.isRead = isRead;
    }
}
