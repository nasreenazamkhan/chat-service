package com.cg.chatservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccessDeniedException extends ChatServiceException {

    public AccessDeniedException(String message) {
        super(message);
    }

    public static AccessDeniedException session(String userId, String uuid) {
        return new AccessDeniedException(
                "User '" + userId + "' does not own session '" + uuid + "'");
    }
}
