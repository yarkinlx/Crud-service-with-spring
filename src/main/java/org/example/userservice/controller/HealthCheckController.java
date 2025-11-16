package org.example.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Tag(name = "Health Check", description = "Health check endpoints for monitoring")
public class HealthCheckController implements HealthIndicator {

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        details.put("service", "user-service");
        details.put("status", "UP");
        details.put("timestamp", System.currentTimeMillis());

        return Health.up().withDetails(details).build();
    }

    @GetMapping("/actuator/health/custom")
    @Operation(summary = "Custom health check", description = "Custom health check endpoint")
    public Map<String, Object> customHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "user-service");
        health.put("timestamp", java.time.LocalDateTime.now().toString());
        return health;
    }
}