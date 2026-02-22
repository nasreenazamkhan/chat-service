package com.cg.chatservice.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request body to rename a chat session")
public class RenameSessionRequest {

    @NotBlank(message = "title must not be blank")
    @Size(min = 1, max = 255, message = "title must be between 1 and 255 characters")
    @Schema(description = "New title for the chat session",
            example = "Renamed Chat", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;
}