package com.cg.chatservice.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-KEY";

    // ── Paths that do NOT require API key ─────────────────────────────────────
    private static final List<String> EXCLUDED_PATHS = List.of(
            "/actuator",
            "/swagger-ui",
            "/swagger-ui.html",
            "/v3/api-docs",
            "/favicon.ico"
    );

    private final SecurityProperties securityProperties;

    /**
     * Skip this filter entirely for Swagger UI, actuator and api-docs paths.
     * Without this, ApiKeyAuthFilter blocks Swagger UI before Spring Security
     * can apply the permitAll() rules configured in SecurityConfig.
     */
    @Override
    protected boolean shouldNotFilter(jakarta.servlet.http.HttpServletRequest request) {
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

        String path = request.getRequestURI();
        String apiKey = request.getHeader(API_KEY_HEADER);

        // ── Missing header ────────────────────────────────────────────────────
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Request rejected - missing API key. Path: {}", path);
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED,
                    "Missing API key. Please provide X-API-KEY header.");
            return;
        }

        // ── Invalid key ───────────────────────────────────────────────────────
        if (!securityProperties.getApiKey().equals(apiKey)) {
            log.warn("Request rejected - invalid API key. Path: {}", path);
            sendErrorResponse(response, HttpStatus.FORBIDDEN, "Invalid API key.");
            return;
        }

        // ── Valid — set Authentication in SecurityContext ─────────────────────
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "api-key-user",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_API"))
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("API key validated. Path: {}", path);
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

    private void sendErrorResponse(HttpServletResponse response,
                                   HttpStatus status,
                                   String message) throws IOException {
        if (response.isCommitted()) return;
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("""
                {
                    "success": false,
                    "message": "%s",
                    "timestamp": "%s"
                }
                """.formatted(message, Instant.now().toString()));
    }
}