package com.cg.chatservice.dto;


import com.cg.chatservice.enums.SenderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

@Data
public class AddMessageRequest {

    @NotNull(message = "senderType is required")
    private SenderType senderType;

    @Size(max = 128)
    private String senderId;        // optional for ASSISTANT / SYSTEM

    @NotBlank(message = "content must not be blank")
    @Size(max = 10_000, message = "content must be at most 10,000 characters")
    private String content;

    /** Arbitrary context metadata (e.g. model name, temperature, citations). */
    private Map<String, Object> contextData;

    private Integer tokenCount;
}