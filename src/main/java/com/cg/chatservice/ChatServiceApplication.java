package com.cg.chatservice;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChatServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatServiceApplication.class, args);
    }

   /* @Value("${app.security.api-key}")
    private String apiKey;

    @PostConstruct
    public void checkConfig() {
        System.out.println("✅ API Key loaded: " + apiKey);
    }*/



}
