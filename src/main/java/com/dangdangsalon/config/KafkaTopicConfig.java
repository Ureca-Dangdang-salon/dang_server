package com.dangdangsalon.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic chatTopic() {
        return TopicBuilder.name("chat-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic eventTopic() {
        return TopicBuilder.name("event-alerts")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
