package com.cg.chatservice.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Binds all app.security.* properties from application.yml
 * Replaces @Value injection which cannot handle List<String> from YAML
 */
@Configuration
@ConfigurationProperties(prefix = "app.security")
@Data
public class SecurityProperties {

    private String apiKey;

    private Cors cors = new Cors();

    @Data
    public static class Cors {

        private List<String> allowedOrigins = List.of("http://localhost:3000");

        private List<String> allowedMethods = List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

        private List<String> allowedHeaders = List.of("*");

        private long maxAgeSeconds = 3600;
    }
}