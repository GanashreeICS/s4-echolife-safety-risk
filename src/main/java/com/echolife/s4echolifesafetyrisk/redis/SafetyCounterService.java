package com.echolife.s4echolifesafetyrisk.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class SafetyCounterService {

    private final StringRedisTemplate redisTemplate;

    public SafetyCounterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public long incrementAndGetDailyMinutes(String tenantId, String userId, int minutesToAdd) {
        String key = buildDailyUsageKey(tenantId, userId);
        Long newTotal = redisTemplate.opsForValue().increment(key, minutesToAdd);

        Long expire = redisTemplate.getExpire(key);
        if (expire == null || expire < 0) {
            redisTemplate.expire(key, calculateDurationUntilMidnight());
        }

        return newTotal != null ? newTotal : minutesToAdd;
    }

    public long getDailyMinutes(String tenantId, String userId) {
        String key = buildDailyUsageKey(tenantId, userId);
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : 0L;
    }

    private String buildDailyUsageKey(String tenantId, String userId) {
        return "usage:" + tenantId + ":" + userId + ":" + LocalDate.now();
    }

    private Duration calculateDurationUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIDNIGHT);
        return Duration.between(now, midnight);
    }
}