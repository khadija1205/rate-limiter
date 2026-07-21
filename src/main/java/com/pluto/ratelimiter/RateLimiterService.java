package com.pluto.ratelimiter;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class RateLimiterService {
    

    private final RedisTemplate<String,String> redisTemplate;
    private static final int LIMIT = 10;
    private static final int WINDOW_SECONDS = 60;

   private static final String LUA_SCRIPT = """
        local current = redis.call('INCR', KEYS[1])
        if current == 1 then
            redis.call('EXPIRE', KEYS[1], ARGV[1])
        end
        if current > tonumber(ARGV[2]) then
            return 0
        end
        return 1
        """;
    public RateLimiterService(RedisTemplate<String,String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean allowRequest(String userId) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(LUA_SCRIPT, Long.class);

        List<String> keys = Collections.singletonList("rate_limit:" + userId);

        Long result = redisTemplate.execute(script, keys,String.valueOf(WINDOW_SECONDS), String.valueOf(LIMIT));

        return result != null && result == 1;
    }

    public int getCurrentCount(String userId) {
        String value = redisTemplate.opsForValue().get("rate_limit:" + userId);
        return value == null ? 0 : Integer.parseInt(value);
    }
   
}