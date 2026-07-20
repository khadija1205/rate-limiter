package com.pluto.ratelimiter;

public class RateLimitExceededException extends RuntimeException {
     public RateLimitExceededException(String message) {
        super(message);
    }   
    
}
