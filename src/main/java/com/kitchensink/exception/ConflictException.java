package com.kitchensink.exception;

import org.springframework.http.HttpStatus;

import com.kitchensink.enums.ErrorType;

public class ConflictException extends BaseApplicationException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 721810209229508222L;

    public ConflictException(String message, ErrorType errorType) {
        super(message, errorType, HttpStatus.CONFLICT);
    }

}
