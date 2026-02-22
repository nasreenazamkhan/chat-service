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
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    // ── Paths that do NOT require rate limiting ────────────────────────────────
    private static final List<String> EXCLUDED_PATHS = List.of(
            "/actuator",
            "/swagger-ui",
            "/swagger-ui.html",
            "/v3/api-docs",
            "/favicon.ico"
    );
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Skip rate limiting for Swagger UI and actuator paths.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // ── Skip if response already committed ────────────────────────────────
        if (response.isCommitted()) {
            return;
        }

        String clientIp = getClientIp(request);
        String redisKey = RATE_LIMIT_PREFIX + clientIp;

        try {
            Long requestCount = redisTemplate.opsForValue().increment(redisKey);

            if (requestCount != null && requestCount == 1) {
                redisTemplate.expire(redisKey, Duration.ofMinutes(1));
            }

            response.setHeader("X-RateLimit-Limit",
                    String.valueOf(MAX_REQUESTS_PER_MINUTE));
            response.setHeader("X-RateLimit-Remaining",
                    String.valueOf(Math.max(0, MAX_REQUESTS_PER_MINUTE -
                            (requestCount != null ? requestCount : 0))));

            if (requestCount != null && requestCount > MAX_REQUESTS_PER_MINUTE) {
                log.warn("Rate limit exceeded for IP: {}", clientIp);
                sendErrorResponse(response);
                return;
            }

        } catch (Exception e) {
            log.error("Rate limit check failed, allowing request: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return true;
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp.trim();
        }
        return request.getRemoteAddr();
    }

    private void sendErrorResponse(HttpServletResponse response) throws IOException {
        if (response.isCommitted()) return;
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