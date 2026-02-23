package com.cg.chatservice.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Request/Response Access Logger.
 *
 * Logs every HTTP request automatically:
 *   → POST   /api/v1/sessions                              | user=user-001
 *   ← POST   /api/v1/sessions           | 201 | 45ms
 *
 * Skips actuator and Swagger paths to reduce noise.
 * Writes to both console and access log file.
 */
@Component
@Order(2)
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    // Dedicated logger that writes to access log file
    private static final Logger ACCESS_LOG = LoggerFactory.getLogger("ACCESS_LOG");

    private static final List<String> SKIP_PATHS = List.of(
            "/actuator",
            "/swagger-ui",
            "/v3/api-docs",
            "/favicon.ico"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Skip logging for noise paths
        if (SKIP_PATHS.stream().anyMatch(path::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        String method = request.getMethod();
        String userId = request.getHeader("X-User-Id");
        long   startTime = System.currentTimeMillis();

        // ── Log incoming request ──────────────────────────────────────────────
        ACCESS_LOG.info("→ {} {} | user={} | ip={}",
                method,
                path,
                userId != null ? userId : "anonymous",
                getClientIp(request));

        try {
            filterChain.doFilter(request, response);
        } finally {
            // ── Log outgoing response ─────────────────────────────────────────
            long duration = System.currentTimeMillis() - startTime;
            int  status   = response.getStatus();

            if (status >= 500) {
                ACCESS_LOG.error("← {} {} | {} | {}ms", method, path, status, duration);
            } else if (status >= 400) {
                ACCESS_LOG.warn("← {} {} | {} | {}ms", method, path, status, duration);
            } else {
                ACCESS_LOG.info("← {} {} | {} | {}ms", method, path, status, duration);
            }
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
