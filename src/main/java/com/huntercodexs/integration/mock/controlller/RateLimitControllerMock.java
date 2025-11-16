package com.huntercodexs.integration.mock.controlller;

import com.huntercodexs.integration.ratelimit.annotation.RateLimit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
public class RateLimitControllerMock {

    @GetMapping("/api/limited")
    @RateLimit(limit = 3, duration = 10, unit = TimeUnit.SECONDS)
    public String limitedEndpoint() {
        return "Request Allowed. Limit: 3/10s.";
    }

    @GetMapping("/api/public")
    public String publicEndpoint() {
        return "Request Allowed. This is a free endpoint.";
    }
}
