package com.pluto.ratelimiter;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;



@Aspect
@Component
public class RateLimiterAspect {

    private final RateLimiterService rateLimiterService;

    public RateLimiterAspect(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }
    
    @Before("@annotation(com.pluto.ratelimiter.RateLimited)")
    public void checkRateLimit(JoinPoint joinPoint) {
        Object[] args  =joinPoint.getArgs();

        String userId = (String) args[0];

        boolean allowed = rateLimiterService.allowRequest(userId);

        if(!allowed) {
            throw new RateLimitExceededException("Rate limit exceeded for user: " + userId);
        }
    }
}
