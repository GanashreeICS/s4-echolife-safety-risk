package com.echolife.s4echolifesafetyrisk.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RedisRateLimiterService {

    private static final Logger log = LoggerFactory.getLogger(RedisRateLimiterService.class);
    private final StringRedisTemplate redisTemplate;

    public RedisRateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Checks if a request is allowed using a sliding window log in Redis.
     *
     * @param keyPrefix     Rate limiting namespace (e.g. "rate:safety:input")
     * @param identifier    Subject being limited (e.g. "tenant-alpha:user-101")
     * @param limit         Maximum allowed operations in the window
     * @param windowSeconds Window length in seconds
     * @return true if the request is within limits, false if throttled
     */
    public boolean isAllowed(String keyPrefix, String identifier, long limit, long windowSeconds) {
        String redisKey = keyPrefix + ":" + identifier;
        long nowMillis = Instant.now().toEpochMilli();
        long windowStartMillis = nowMillis - (windowSeconds * 1000);

        try {
            // 1. Remove events older than the current rolling window
            redisTemplate.opsForZSet().removeRangeByScore(redisKey, 0, windowStartMillis);

            // 2. Count requests executed within the active window
            Long count = redisTemplate.opsForZSet().zCard(redisKey);

            if (count != null && count >= limit) {
                log.warn("Rate limit breached for key='{}' (count={}/limit={})", redisKey, count, limit);
                return false;
            }

            // 3. Record the current request with its timestamp as the score
            String uniqueMember = nowMillis + "-" + UUID.randomUUID().toString().substring(0, 8);
            redisTemplate.opsForZSet().add(redisKey, uniqueMember, nowMillis);

            // 4. Set TTL on the key so inactive keys cleanly expire
            redisTemplate.expire(redisKey, windowSeconds + 10, TimeUnit.SECONDS);

            return true;
        } catch (Exception ex) {
            log.error("Redis rate limiter lookup failed for '{}', failing open: {}", redisKey, ex.getMessage());
            // Fail open: Allow the request so a Redis blip does not bring down safety checks
            return true;
        }
    }
}