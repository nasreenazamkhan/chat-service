package com.cg.chatservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RenameSessionRequest {

    @NotBlank(message = "title must not be blank")
    @Size(min = 1, max = 255, message = "title must be between 1 and 255 characters")
    private String title;
}
