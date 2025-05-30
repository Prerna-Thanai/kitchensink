package com.kitchensink.exception;

import org.springframework.http.HttpStatus;

import com.kitchensink.enums.ErrorType;

public class UserNotFoundException extends BaseApplicationException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 721810209229508444L;

    /**
     * Instantiates a new authentication exception.
     *
     * @param message
     *            the message
     * @param errorType
     *            the errorType
     */
    public UserNotFoundException(String message, ErrorType errorType) {
        super(message, errorType, HttpStatus.NOT_FOUND);
    }

}
