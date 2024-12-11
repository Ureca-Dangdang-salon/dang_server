package com.dangdangsalon.domain.coupon.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Repository
@RequiredArgsConstructor
public class ZSetRedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 대기열에 값을 추가
     */
    public void addIfAbsent(String key, Object value, double score, int expireDays) {
        redisTemplate.opsForZSet().addIfAbsent(requireNonNull(key), requireNonNull(value), score);
        redisTemplate.expire(key, Duration.ofDays(expireDays));
    }

    /**
     * 주어진 범위의 값 반환
     */
    public Set<Long> range(String key, long start, long end) {
        return requireNonNull(redisTemplate.opsForZSet().range(requireNonNull(key), start, end))
                .stream()
                .map(memberId -> Long.parseLong(String.valueOf(memberId)))
                .collect(Collectors.toSet());
    }

    /**
     * 특정 값의 순위 반환
     */
    public Long rank(String key, Object value) {
        return redisTemplate.opsForZSet().rank(key, value);
    }


    /**
     * 특정 값의 점수 확인
     */
    public Double score(String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    /**
     * 전체 크기 반환
     */
    public Long size(String key) {
        return redisTemplate.opsForZSet().zCard(key);
    }

    /**
     * 특정 값 삭제
     */
    public void delete(String key, Object value) {
        Long remove = redisTemplate.opsForZSet().remove(key, value);
        System.out.println("Removed count: " + remove);
    }

}
