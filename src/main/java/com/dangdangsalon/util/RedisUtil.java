package com.dangdangsalon.util;

import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    public void setValue(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
        log.info("Set value - Redis, key: {}", key);
    }

    public Object getValue(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        log.info("Get value - Redis, key: {}", key);
        return value;
    }

    public void pushToList(String listKey, Object value) {
        redisTemplate.opsForList().leftPush(listKey, value);
        log.info("Push value - Redis list: {}", listKey);
    }

    public List<Object> getAllFromList(String listKey) {
        List<Object> values = redisTemplate.opsForList().range(listKey, 0, -1);
        log.info("Get All value - Redis list: {}", listKey);
        return values;
    }

    public void deleteList(String listKey) {
        redisTemplate.delete(listKey);
        log.info("Delete Redis list: {}", listKey);
    }

    public void saveRefreshToken(String userId, String refreshToken, long duration) {
        redisTemplate.opsForValue().set(userId, refreshToken, duration, TimeUnit.MILLISECONDS);
    }

    public Object getRefreshToken(String userId) {
        return redisTemplate.opsForValue().get(userId);
    }

    public void deleteRefreshToken(String userId) {
        redisTemplate.delete(userId);
    }
}
