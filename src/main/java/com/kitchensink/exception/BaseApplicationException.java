package com.kitchensink.exception;

import org.springframework.http.HttpStatus;

import com.kitchensink.enums.ErrorType;

public class BaseApplicationException extends RuntimeException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The error type */
    private final ErrorType errorType;

    /** The status */
    private final HttpStatus status;

    /**
     * Instantiates BaseApplicationException
     *
     * @param message
     *            the message
     * @param errorType
     *            the error type
     * @param status
     *            the status
     */
    public BaseApplicationException(String message, ErrorType errorType, HttpStatus status) {
        super(message);
        this.errorType = errorType;
        this.status = status;
    }

    /**
     * BaseApplicationException
     *
     * @param message
     *            the message
     * @param cause
     *            the cause
     * @param errorType
     *            the error type
     * @param status
     *            the status
     */
    public BaseApplicationException(String message, Throwable cause, ErrorType errorType, HttpStatus status) {
        super(message, cause);
        this.errorType = errorType;
        this.status = status;
    }

    /**
     * @return the errorType
     */
    public ErrorType getErrorType() {
        return errorType;
    }

    /**
     * @return the status
     */
    public HttpStatus getStatus() {
        return status;
    }

}