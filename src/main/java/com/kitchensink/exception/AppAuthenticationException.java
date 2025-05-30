package com.kitchensink.exception;

import org.springframework.http.HttpStatus;

import com.kitchensink.enums.ErrorType;

public class AppAuthenticationException extends BaseApplicationException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 721810209229508111L;

    public AppAuthenticationException(String message, ErrorType errorType) {
        super(message, errorType, HttpStatus.UNAUTHORIZED);
    }
}
