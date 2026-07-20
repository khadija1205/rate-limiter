package com.pluto.ratelimiter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;



@SpringBootTest
public class RateLimiterConcurrencyTest {
    
    @Autowired
    private RateLimiterService rateLimiterService;

    @Test
    public void testConcurrentRequests_shouldNotExceedLimit() throws InterruptedException { 
        String userId = "concurrent-test-user";

        int numberOfThreads = 200;

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger allowedCount = new AtomicInteger(0);


        for(int i = 0; i < numberOfThreads; i++){
            executor.submit(() -> {
                try {
                    boolean allowed = rateLimiterService.allowRequest(userId);
                    if(allowed) {
                        allowedCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        System.out.println("Total requests allowed: " + allowedCount.get());
        System.out.println("Expected limit: 10");


        assert allowedCount.get() <= 10 :
            "Rate limiter allowed " + allowedCount.get() + "requests, expected max 10!";
    }
}
