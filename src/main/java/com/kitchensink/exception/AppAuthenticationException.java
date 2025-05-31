package com.kitchensink.exception;

import org.springframework.http.HttpStatus;

import com.kitchensink.enums.ErrorType;

/**
 * The Class AppAuthenticationException.
 *
 * @author prerna
 */
public class AppAuthenticationException extends BaseApplicationException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 721810209229508111L;

    /**
     * Instantiates AppAuthenticationException
     *
     * @param message
     *            the message
     * @param errorType
     *            the error type
     */
    public AppAuthenticationException(String message, ErrorType errorType) {
        super(message, errorType, HttpStatus.UNAUTHORIZED);
    }
}
