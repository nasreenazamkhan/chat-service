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
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-KEY";

    private final SecurityProperties securityProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // ── Skip actuator endpoints ───────────────────────────────────────────
        String path = request.getRequestURI();
        if (path.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader(API_KEY_HEADER);

        // ── Missing header ────────────────────────────────────────────────────
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Request rejected - missing API key. Path: {}", path);
            sendErrorResponse(response,
                    HttpStatus.UNAUTHORIZED,
                    "Missing API key. Please provide X-API-KEY header.");
            return;
        }

        // ── Invalid key ───────────────────────────────────────────────────────
        if (!securityProperties.getApiKey().equals(apiKey)) {
            log.warn("Request rejected - invalid API key. Path: {}", path);
            sendErrorResponse(response,
                    HttpStatus.FORBIDDEN,
                    "Invalid API key.");
            return;
        }


        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "api-key-user",           // principal
                        null,                     // credentials
                        List.of(new SimpleGrantedAuthority("ROLE_API"))  // authorities
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("API key validated. SecurityContext updated. Path: {}", path);
        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response,
                                   HttpStatus status,
                                   String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("""
                {
                    "success": false,
                    "message": "%s",
                    "timestamp": "%s"
                }
                """.formatted(message, java.time.Instant.now().toString()));
    }
}

