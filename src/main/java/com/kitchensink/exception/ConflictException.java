package com.kitchensink.exception;

import com.kitchensink.enums.ErrorType;
import org.springframework.http.HttpStatus;

public class ConflictException extends BaseApplicationException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 721810209229508222L;

    /**
     * Instantiates ConflictException
     *
     * @param message
     *            the message
     * @param errorType
     *            the error type
     */
    public ConflictException(String message, ErrorType errorType) {
        super(message, errorType, HttpStatus.CONFLICT);
    }

}
