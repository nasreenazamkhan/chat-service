package com.cg.chatservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.chat")
@Data
public class AppProperties {

    private int maxMessageLength    = 10_000;
    private int maxSessionsPerUser  = 500;
    private int messagePageSize     = 50;
}