package com.dangdangsalon.config;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.GenericContainer;

@SpringBootTest
@Profile("test")
public class RedisTest {

    private static GenericContainer<?> redisContainer;

    @BeforeAll
    public static void startContainer() {
        redisContainer = new GenericContainer<>("redis:7.0.0")
                .withExposedPorts(6379);
        redisContainer.start();
        System.setProperty("spring.redis.host", redisContainer.getHost());
        System.setProperty("spring.redis.port", redisContainer.getMappedPort(6379).toString());
    }

    @AfterAll
    public static void stopContainer() {
        if (redisContainer != null) {
            redisContainer.stop();
        }
    }
}

