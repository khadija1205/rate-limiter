package com.pluto.ratelimiter;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class RateLimiterService {
    private final Map<String, Integer> requestCounts = new HashMap<>();
    private static final int LIMIT = 10;


    public boolean allowRequest(String userId) {
        int currentCount = requestCounts.getOrDefault(userId, 0);

        if(currentCount >= LIMIT) {
            return false;
        }

        requestCounts.put(userId, currentCount + 1);
        return true;

    }

    public int getCurrentCount(String userId) { 
        return requestCounts.getOrDefault(userId, 0);
    }
}