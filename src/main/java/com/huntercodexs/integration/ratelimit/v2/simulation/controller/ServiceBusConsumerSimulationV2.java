package com.huntercodexs.integration.ratelimit.v2.simulation.controller;

import com.huntercodexs.integration.ratelimit.v2.annotation.RateLimitServiceBusV2;
import com.huntercodexs.integration.ratelimit.v2.simulation.dto.ProcessMessageV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
public class ServiceBusConsumerSimulationV2 {

    private static final Logger log = LoggerFactory.getLogger(ServiceBusConsumerSimulationV2.class);

    @PostMapping("/simulate-queue-process")
    @RateLimitServiceBusV2(limit = 5, duration = 10, unit = TimeUnit.SECONDS, keyParameterName = "message")
    public ResponseEntity<String> processMessage(@RequestBody ProcessMessageV2 message) {
        log.info("Processing message for UserID: {}", message.getUserId());
        return ResponseEntity.ok("Message processed successfully for userId: " + message.getUserId());
    }
}
