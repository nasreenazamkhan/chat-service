package com.cg.chatservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

/**
 * Rate limiting filter to protect against brute force attacks.
 * Limits each IP to 100 requests per minute using Redis counters.
 * <p>
 * If attacker tries to brute-force sessionUuid or userId,
 * they will be blocked after 100 attempts per minute.
 */
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // ── Skip actuator ─────────────────────────────────────────────────────
        if (request.getRequestURI().startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        String redisKey = RATE_LIMIT_PREFIX + clientIp;

        try {
            Long requestCount = redisTemplate.opsForValue().increment(redisKey);

            // Set expiry on first request only
            if (requestCount != null && requestCount == 1) {
                redisTemplate.expire(redisKey, Duration.ofMinutes(1));
            }

            // ── Add rate limit headers so client knows their limits ────────────
            response.setHeader("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS_PER_MINUTE));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(
                    Math.max(0, MAX_REQUESTS_PER_MINUTE - (requestCount != null ? requestCount : 0))));

            // ── Block if limit exceeded ────────────────────────────────────────
            if (requestCount != null && requestCount > MAX_REQUESTS_PER_MINUTE) {
                log.warn("Rate limit exceeded for IP: {}", clientIp);
                sendErrorResponse(response);
                return;
            }

        } catch (Exception e) {
            // If Redis is down, allow request through (fail open)
            log.error("Rate limit check failed (Redis issue), allowing request: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts real client IP, handles proxies and load balancers.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // X-Forwarded-For can contain multiple IPs — take the first (original client)
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }

    private void sendErrorResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", "60");
        response.getWriter().write("""
                {
                    "success": false,
                    "message": "Too many requests. Please try again after 60 seconds.",
                    "timestamp": "%s"
                }
                """.formatted(Instant.now().toString()));
    }
}
