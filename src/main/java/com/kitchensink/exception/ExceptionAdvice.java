package com.kitchensink.exception;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

// @ControllerAdvice
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

    public ExceptionAdvice() {
        super();
    }

    @ExceptionHandler({ ConflictException.class })
    public ResponseEntity<Object> handleException(ConflictException exception, WebRequest request) {
        return super.handleExceptionInternal(exception, Map.of("message", exception.getMessage()), HttpHeaders.EMPTY,
            HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler({ AuthenticationException.class })
    public ResponseEntity<Object> handleException(AuthenticationException exception, WebRequest request) {
        return super.handleExceptionInternal(exception, Map.of("message", exception.getMessage()), HttpHeaders.EMPTY,
            HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler({ UsernameNotFoundException.class, BadCredentialsException.class })
    public ResponseEntity<Object> handleMemberLoginException(Exception exception, WebRequest request) {
        return super.handleExceptionInternal(exception, Map.of("message", "Invalid email id or password"),
            HttpHeaders.EMPTY, HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler({ Exception.class })
    public ResponseEntity<Object> handleUncaughtExceptions(Exception exception, WebRequest request) {
        logger.error("Unhandled exception occurred: ", exception);
        return super.handleExceptionInternal(exception, Map.of("message", "Unable to process the request. Please try "
            + "again later."), HttpHeaders.EMPTY, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
