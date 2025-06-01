package com.kitchensink.exception;

import com.kitchensink.enums.ErrorType;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BaseApplicationException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 721810209229508444L;

    /**
     * Instantiates UserNotFoundException
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
