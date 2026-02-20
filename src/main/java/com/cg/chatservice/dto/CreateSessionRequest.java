package com.cg.chatservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateSessionRequest {

    @NotBlank(message = "userId must not be blank")
    @Size(max = 128, message = "userId must be at most 128 characters")
    private String userId;

    @Size(max = 255, message = "title must be at most 255 characters")
    private String title;   // optional – defaults to "New Chat"
}
