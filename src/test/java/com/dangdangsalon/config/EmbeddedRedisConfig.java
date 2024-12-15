package com.dangdangsalon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

import java.io.IOException;

@Configuration
@Profile("test") // 'test' 프로필에서만 활성화
public class EmbeddedRedisConfig {

    private RedisServer redisServer;

    @Bean
    public RedisServer redisServer() throws IOException {
        redisServer = new RedisServer(6379); // 테스트용 Redis 포트
        redisServer.start();
        return redisServer;
    }

    // 테스트 종료 후 Redis 서버를 종료
    @Bean
    public void stopRedis() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (redisServer != null) {
                redisServer.stop();
            }
        }));
    }
}
