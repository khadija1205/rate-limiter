package com.pluto.ratelimiter;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimiterService {
    private final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private static final int LIMIT = 10;


    public boolean allowRequest(String userId) {
        AtomicInteger counter = requestCounts.computeIfAbsent(userId, key -> new AtomicInteger(0));


        int newCount = counter.incrementAndGet();

        if(newCount > LIMIT) {
           counter.decrementAndGet();
           return false;
        }


        return true;

    }

    public int getCurrentCount(String userId) { 
        AtomicInteger counter = requestCounts.get(userId);
        return counter == null ? 0 : counter.get();
    }
}