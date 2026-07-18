package com.Uber.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class RateLimiterService {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final RedisScript<Long> rateLimitScript;

    public RateLimiterService(
            @Qualifier("reactiveRedisTemplate")ReactiveRedisTemplate<String, String> redisTemplate, RedisScript<Long> rateLimitScript) {
        this.redisTemplate = redisTemplate;
        this.rateLimitScript = rateLimitScript;
    }

    public Mono<Boolean> isAllowed(String clientIp, int maxLimit, long windowMillis) {
        String key = "rate:" + clientIp;
        String now = String.valueOf(System.currentTimeMillis());
        String window = String.valueOf(windowMillis);
        String limit = String.valueOf(maxLimit);
        String requestId = UUID.randomUUID().toString();

        List<String> keys = Collections.singletonList(key);

        return redisTemplate.execute(rateLimitScript, keys, List.of(now, window, limit, requestId))
                .next()
                .map(result -> result == 1L);
    }
}