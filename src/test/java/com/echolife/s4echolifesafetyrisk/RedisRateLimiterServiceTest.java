package com.echolife.s4echolifesafetyrisk;

import com.echolife.s4echolifesafetyrisk.service.RedisRateLimiterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"safety-risk-events", "safety-risk-events.DLQ"})
class RedisRateLimiterServiceTest {

    @Autowired
    private RedisRateLimiterService rateLimiterService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void cleanRedis() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }

    @Test
    @DisplayName("Should permit requests within limit and throttle subsequent requests")
    void testSlidingWindowRateLimiting() {
        String testUser = "tenant-unit:user-test";
        long limit = 3;
        long windowSec = 10;

        // First 3 requests should pass
        assertThat(rateLimiterService.isAllowed("rate:unit:test", testUser, limit, windowSec)).isTrue();
        assertThat(rateLimiterService.isAllowed("rate:unit:test", testUser, limit, windowSec)).isTrue();
        assertThat(rateLimiterService.isAllowed("rate:unit:test", testUser, limit, windowSec)).isTrue();

        // 4th request exceeds the limit of 3
        assertThat(rateLimiterService.isAllowed("rate:unit:test", testUser, limit, windowSec)).isFalse();
    }
}