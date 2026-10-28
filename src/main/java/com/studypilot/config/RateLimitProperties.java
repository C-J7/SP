package com.studypilot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.rate-limit")
public record RateLimitProperties(int capacity, int refillPerMinute) {
}
