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
        redisServer = new RedisServer(6379); // 포트를 변경하려면 여기서 수정
        redisServer.start();
        return redisServer;
    }

    // 테스트 종료 시 Redis 서버를 종료
    @Bean
    public void stopRedis() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (redisServer != null) {
                redisServer.stop();
            }
        }));
    }
}
