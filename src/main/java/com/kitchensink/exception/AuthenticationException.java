package com.kitchensink.exception;

import com.kitchensink.enums.ErrorType;

public class AuthenticationException extends BaseApplicationException {
    public AuthenticationException(String message, ErrorType errorType) {
        super(message, errorType);
    }
}
