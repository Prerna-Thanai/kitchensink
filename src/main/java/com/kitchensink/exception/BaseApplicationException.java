package com.kitchensink.exception;

import com.kitchensink.enums.ErrorType;

public class BaseApplicationException extends RuntimeException {
    private final ErrorType errorType;

    public BaseApplicationException(String message, ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }
    public BaseApplicationException(String message, Throwable cause, ErrorType errorType) {
        super(message, cause);
        this.errorType = errorType;
    }
}
