package com.cg.chatservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends ChatServiceException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException session(String uuid) {
        return new ResourceNotFoundException("Chat session not found: " + uuid);
    }

    public static ResourceNotFoundException message(String uuid) {
        return new ResourceNotFoundException("Chat message not found: " + uuid);
    }
}
