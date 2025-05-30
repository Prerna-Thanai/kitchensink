package com.kitchensink.exception;

import org.springframework.http.HttpStatus;

import com.kitchensink.enums.ErrorType;

public class KitchensinkBusinessException extends BaseApplicationException {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 721810209229508333L;

    public KitchensinkBusinessException(String message, ErrorType errorType) {
        super(message, errorType, HttpStatus.BAD_REQUEST);
    }

}
