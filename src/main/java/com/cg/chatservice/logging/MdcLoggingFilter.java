package com.cg.chatservice.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * MDC (Mapped Diagnostic Context) Filter.
 *
 * Adds the following fields to EVERY log line automatically:
 *   - requestId : unique ID per HTTP request (for tracing)
 *   - userId    : from X-User-Id header
 *
 * Example log output:
 *   2026-02-22 10:00:01 INFO [reqId=abc-123] [user=user-001] ChatSessionService - Session created
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcLoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String USER_ID_HEADER    = "X-User-Id";
    private static final String MDC_REQUEST_ID    = "requestId";
    private static final String MDC_USER_ID       = "userId";
    private static final String MDC_METHOD        = "method";
    private static final String MDC_PATH          = "path";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // ── Set MDC values ────────────────────────────────────────────────
            String requestId = request.getHeader(REQUEST_ID_HEADER);
            if (requestId == null || requestId.isBlank()) {
                requestId = UUID.randomUUID().toString().substring(0, 8);
            }

            String userId = request.getHeader(USER_ID_HEADER);

            MDC.put(MDC_REQUEST_ID, requestId);
            MDC.put(MDC_USER_ID,    userId != null ? userId : "anonymous");
            MDC.put(MDC_METHOD,     request.getMethod());
            MDC.put(MDC_PATH,       request.getRequestURI());

            // ── Add requestId to response header for client tracing ───────────
            response.setHeader(REQUEST_ID_HEADER, requestId);

            filterChain.doFilter(request, response);

        } finally {
            // ── Always clear MDC to prevent thread pool leakage ───────────────
            MDC.clear();
        }
    }
}
