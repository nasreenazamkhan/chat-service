package com.cg.chatservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class SessionLimitExceededException extends ChatServiceException {

    public SessionLimitExceededException(long max) {
        super("Session limit of " + max + " reached for this user");
    }
}
