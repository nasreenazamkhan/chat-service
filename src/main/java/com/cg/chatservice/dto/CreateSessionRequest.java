package com.cg.chatservice.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request body to create a new chat session")
public class CreateSessionRequest {

    @NotBlank(message = "userId must not be blank")
    @Size(max = 128, message = "userId must be at most 128 characters")
    @Schema(description = "Unique identifier of the user",
            example = "user-001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userId;

    @Size(max = 255, message = "title must be at most 255 characters")
    @Schema(description = "Title of the chat session. Defaults to 'New Chat' if not provided",
            example = "My First Chat", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String title;
}
