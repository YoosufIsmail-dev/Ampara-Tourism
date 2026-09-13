package com.ampara.tourism.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lightweight per-instance rate limiter. It protects public APIs from accidental
 * request storms. In a multi-instance deployment, keep a WAF/CDN rate limit in
 * front of the service as the global enforcement layer.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final int requestsPerMinute;
    private final int authRequestsPerMinute;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimitFilter(
            @Value("${app.rate-limit.requests-per-minute:120}") int requestsPerMinute,
            @Value("${app.rate-limit.auth-requests-per-minute:20}") int authRequestsPerMinute) {
        this.requestsPerMinute = Math.max(10, requestsPerMinute);
        this.authRequestsPerMinute = Math.max(5, authRequestsPerMinute);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path.startsWith("/actuator/health") || path.startsWith("/css/") || path.startsWith("/js/")
                || path.startsWith("/assets/") || path.startsWith("/images/") || path.equals("/favicon.ico")) {
            filterChain.doFilter(request, response);
            return;
        }

        String client = clientKey(request);
        boolean auth = path.startsWith("/api/auth/");
        int limit = auth ? authRequestsPerMinute : requestsPerMinute;
        String key = (auth ? "auth:" : "api:") + client;
        Window window = windows.computeIfAbsent(key, k -> new Window());
        long now = System.currentTimeMillis();
        synchronized (window) {
            if (now - window.startedAt >= Duration.ofMinutes(1).toMillis()) {
                window.startedAt = now;
                window.count.set(0);
            }
            int current = window.count.incrementAndGet();
            response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, limit - current)));
            if (current > limit) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setHeader("Retry-After", "60");
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"Too many requests. Please try again shortly.\"}");
                return;
            }
        }
        if (windows.size() > 10000) windows.entrySet().removeIf(e -> now - e.getValue().startedAt > 120_000);
        filterChain.doFilter(request, response);
    }

    private String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        return request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
    }

    private static final class Window {
        private volatile long startedAt = System.currentTimeMillis();
        private final AtomicInteger count = new AtomicInteger();
    }
}
