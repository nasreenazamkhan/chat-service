package com.cg.chatservice.dto;


import com.cg.chatservice.enums.SenderType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "Request body to add a message to a chat session")
public class AddMessageRequest {

    @NotNull(message = "senderType is required")
    @Schema(description = "Type of the message sender",
            example = "USER", requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"USER", "ASSISTANT", "SYSTEM"})
    private SenderType senderType;

    @Size(max = 128)
    @Schema(description = "ID of the sender. Optional for ASSISTANT and SYSTEM messages",
            example = "user-001", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String senderId;

    @NotBlank(message = "content must not be blank")
    @Size(max = 10_000, message = "content must be at most 10,000 characters")
    @Schema(description = "Content of the message",
            example = "Hello, how are you?", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "Optional metadata — model name, temperature, citations, etc.",
            example = "{\"model\": \"gpt-4\", \"temperature\": 0.7}",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Map<String, Object> contextData;

    @Schema(description = "Optional token count for cost tracking",
            example = "42", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer tokenCount;
}