package com.valorant.devopshub.controller;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minimal liveness/health endpoint used by:
 *  - Docker HEALTHCHECK (Dockerfile)
 *  - docker-compose healthcheck
 *  - scripts/health-check.sh
 *  - the Jenkins pipeline's "Health Check" stage
 *
 * Deliberately kept dependency-free (no Actuator) so its behavior is easy to
 * read and reason about end-to-end for L1 practice.
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("timestamp", Instant.now().toString());
        return body;
    }
}
