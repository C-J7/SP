package com.studypilot.config;

import java.io.IOException;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import com.studypilot.common.RequestIdFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Per-client token bucket on the API surface. Health checks and docs are never throttled.
 * shortcut: buckets live in this JVM, so limits are per instance; move to Redis (e.g. Bucket4j) before scaling out.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties props;
    private final Clock clock;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(RateLimitProperties props, Clock clock) {
        this.props = props;
        this.clock = clock;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (buckets.size() > 10_000) {
            buckets.clear(); // bound memory under an address-spraying client
        }
        Bucket bucket = buckets.computeIfAbsent(request.getRemoteAddr(), k -> new Bucket(props.capacity(), clock.millis()));
        long retryAfterSeconds = bucket.tryConsume(props.capacity(), props.refillPerMinute(), clock.millis());
        if (retryAfterSeconds == 0) {
            chain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write("""
                {"type":"https://github.com/C-J7/SP/blob/main/docs/errors.md#rate-limited","title":"Too Many Requests",\
                "status":429,"detail":"Rate limit exceeded. Retry after %d seconds.","requestId":"%s"}"""
                .formatted(retryAfterSeconds, RequestIdFilter.current()));
    }

    static final class Bucket {
        private double tokens;
        private long lastRefillMillis;

        Bucket(int capacity, long now) {
            this.tokens = capacity;
            this.lastRefillMillis = now;
        }

        /** Returns 0 when a token was taken, otherwise the seconds until one is available. */
        synchronized long tryConsume(int capacity, int refillPerMinute, long now) {
            double perMilli = refillPerMinute / 60_000.0;
            tokens = Math.min(capacity, tokens + (now - lastRefillMillis) * perMilli);
            lastRefillMillis = now;
            if (tokens >= 1) {
                tokens -= 1;
                return 0;
            }
            return Math.max(1, (long) Math.ceil((1 - tokens) / perMilli / 1000));
        }
    }
}
