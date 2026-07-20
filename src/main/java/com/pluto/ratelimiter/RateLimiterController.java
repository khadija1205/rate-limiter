package com.pluto.ratelimiter;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RateLimiterController {

    private final RateLimiterService rateLimiterService;

    @GetMapping("/api/check")
    @RateLimited
    public String check(@RequestParam String userId) {
        int count = rateLimiterService.getCurrentCount(userId);
        return "Request processed. Current count = " + count;
    }
}