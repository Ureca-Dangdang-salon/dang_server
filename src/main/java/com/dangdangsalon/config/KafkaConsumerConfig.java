//package com.dangdangsalon.config;
//
//import com.dangdangsalon.domain.notification.dto.EventNotificationDto;
//import org.apache.kafka.clients.consumer.ConsumerConfig;
//import org.apache.kafka.common.serialization.StringDeserializer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.annotation.EnableKafka;
//import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
//import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
//import org.springframework.kafka.support.serializer.JsonDeserializer;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@EnableKafka
//@Configuration
//public class KafkaConsumerConfig {
//
//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, EventNotificationDto> kafkaListenerContainerFactory() {
//        ConcurrentKafkaListenerContainerFactory<String, EventNotificationDto> factory =
//                new ConcurrentKafkaListenerContainerFactory<>();
//
//        factory.setConsumerFactory(consumerFactory());
//        return factory;
//    }
//
//    @Bean
//    public DefaultKafkaConsumerFactory<String, EventNotificationDto> consumerFactory() {
//        Map<String, Object> config = new HashMap<>();
//        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
//        config.put(ConsumerConfig.GROUP_ID_CONFIG, "event-alerts-group");
//        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
//
//        return new DefaultKafkaConsumerFactory<>(config,
//                new StringDeserializer(),
//                new JsonDeserializer<>(EventNotificationDto.class));
//    }
//}
