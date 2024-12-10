package com.dangdangsalon.domain.notification.service;

import com.dangdangsalon.domain.notification.dto.EventNotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventNotificationProducer {

    private final KafkaTemplate<String, EventNotificationDto> kafkaTemplate;

    public void sendEventNotification(EventNotificationDto notification) {
        kafkaTemplate.send("event-alerts", notification);
    }
}