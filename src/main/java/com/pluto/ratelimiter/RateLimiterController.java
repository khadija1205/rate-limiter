package com.pluto.ratelimiter;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class RateLimiterController {

    private final RateLimiterService rateLimiterService;

    public RateLimiterController(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }



    @GetMapping("/api/check")
    public String check(@RequestParam String userId) {
        boolean allowed = rateLimiterService.allowRequest(userId);
        int count = rateLimiterService.getCurrentCount(userId);

        if(allowed){
            return "ALLOWED (count = " + count + ")";
        } else {
            return "BLOCKED (limit reached)";
        }
    }
}
