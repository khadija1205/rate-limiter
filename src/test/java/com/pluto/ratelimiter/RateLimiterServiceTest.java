package com.pluto.ratelimiter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimiterServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RateLimiterService rateLimiterService;

    @Test
    void allowRequest_returnsTrue_whenUnderLimit() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(), any()))
                .thenReturn(1L);

        boolean result = rateLimiterService.allowRequest("user1");

        assertTrue(result);
    }

    @Test
    void allowRequest_returnsFalse_whenOverLimit() {
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any(), any()))
                .thenReturn(0L);

        boolean result = rateLimiterService.allowRequest("user1");

        assertFalse(result);
    }

    @Test
    void getCurrentCount_returnsZero_whenNoKeyExists() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("rate_limit:user1")).thenReturn(null);

        int count = rateLimiterService.getCurrentCount("user1");

        assertEquals(0, count);
    }

    @Test
    void getCurrentCount_returnsParsedValue_whenKeyExists() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("rate_limit:user1")).thenReturn("5");

        int count = rateLimiterService.getCurrentCount("user1");

        assertEquals(5, count);
    }
}