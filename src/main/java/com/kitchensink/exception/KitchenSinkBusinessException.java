package com.kitchensink.exception;

import com.kitchensink.enums.ErrorType;
import org.springframework.http.HttpStatus;

public class KitchenSinkBusinessException extends BaseApplicationException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 721810209229508333L;

    /**
     * Instantiates KitchenSinkBusinessException
     *
     * @param message
     *            the message
     * @param errorType
     *            the error type
     */
    public KitchenSinkBusinessException(String message, ErrorType errorType) {
        super(message, errorType, HttpStatus.BAD_REQUEST);
    }

}
