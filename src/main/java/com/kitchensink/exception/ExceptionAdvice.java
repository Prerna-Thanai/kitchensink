package com.kitchensink.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.kitchensink.enums.ErrorType;

import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class ExceptionAdvice.
 *
 * @author prerna
 */
@ControllerAdvice
@Slf4j
public class ExceptionAdvice {

    /**
     * Exception advice constructor
     */
    public ExceptionAdvice() {
        super();
    }

    /**
     * Handle BaseApplication Exception
     *
     * @param exception
     *            the exception
     * @param request
     *            the request
     * @return response entity
     */
    @ExceptionHandler(BaseApplicationException.class)
    public ResponseEntity<Object> handleException(BaseApplicationException exception, WebRequest request) {
        return handleException(exception, exception.getMessage(), exception.getErrorType(), exception.getStatus());
    }

    /**
     * Handle MemberLogin Exception
     *
     * @param exception
     *            the exception
     * @param request
     *            the request
     * @return response entity
     */
    @ExceptionHandler({ UsernameNotFoundException.class, BadCredentialsException.class })
    public ResponseEntity<Object> handleMemberLoginException(Exception exception, WebRequest request) {
        return handleException(exception, "Invalid email id or password", ErrorType.INVALID_CREDENTIALS,
            HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle NoResourceFound Exception
     *
     * @param exception
     *            the exception
     * @param request
     *            the request
     * @return response entity
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException exception,
        WebRequest request) {
        return handleException(exception, "Resource not found", ErrorType.NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle Uncaught Exception
     *
     * @param exception
     *            the exception
     * @param request
     *            the request
     * @return response entity
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUncaughtExceptions(Exception exception, WebRequest request) {
        return handleException(exception, "Unable to process the request. Please try again later. ", ErrorType.UNKNOWN,
            HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle Bind Exception
     *
     * @param exception
     *            the exception
     * @param request
     *            the request
     * @return response entity
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Object> handleBindExceptions(BindException exception, WebRequest request) {
        String errors = exception.getAllErrors().stream().map(ObjectError::getDefaultMessage).filter(err -> err != null
            && !err.isBlank()).collect(Collectors.joining(", "));
        return handleException(exception, errors, ErrorType.REQUEST_VALIDATION_FAILED, BAD_REQUEST);
    }

    /**
     * Handle Method Validation Exception
     *
     * @param exception
     *            the exception
     * @param request
     *            the request
     * @return response entity
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
        @Nonnull HttpHeaders headers, @Nonnull HttpStatusCode status, @Nonnull WebRequest request) {
        String errors = ex.getAllValidationResults().stream().flatMap(result -> result.getResolvableErrors().stream())
            .map(MessageSourceResolvable::getDefaultMessage).collect(Collectors.joining(", "));

        return handleException(ex, errors, ErrorType.REQUEST_VALIDATION_FAILED, ex.getStatusCode());
    }

    /**
     * Handle MessageNotReadable Exception
     *
     * @param exception
     *            the exception
     * @param request
     *            the request
     * @return response entity
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadable(@Nonnull HttpMessageNotReadableException exception,
        @Nonnull HttpHeaders headers, @Nonnull HttpStatusCode status, @Nonnull WebRequest request) {
        final String message;
        if (exception.getCause()instanceof JsonMappingException jsonMappingException) {
            String path = jsonMappingException.getPath().stream().map(ref -> {
                if (ref.getFieldName() == null && ref.getIndex() > -1) {
                    return String.format("[%d]", ref.getIndex());
                }
                return String.valueOf(ref.getFieldName());
            }).collect(Collectors.joining("."));
            message = String.format("Unable to parse %s field", path);
        } else if (exception.getMessage().contains("Required request body is missing")) {
            message = "Required request body is missing";
        } else {
            message = "Unable to read body";
        }
        return handleException(exception, message, ErrorType.REQUEST_VALIDATION_FAILED, BAD_REQUEST);
    }

    /**
     * Handle MediaTypeNotSupported Exception
     *
     * @param exception
     *            the exception
     * @param request
     *            the request
     * @return response entity
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
        @Nonnull HttpMediaTypeNotSupportedException exception, @Nonnull HttpHeaders headers,
        @Nonnull HttpStatusCode status, @Nonnull WebRequest request) {
        return handleException(exception, "Unsupported Media Type", ErrorType.REQUEST_VALIDATION_FAILED,
            UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * Handle Method not supported Exception
     *
     * @param exception
     *            the exception
     * @param request
     *            the request
     * @return response entity
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
        @Nonnull HttpRequestMethodNotSupportedException exception, @Nonnull HttpHeaders headers,
        @Nonnull HttpStatusCode status, @Nonnull WebRequest request) {
        return handleException(exception, "Request Type not supported", ErrorType.REQUEST_VALIDATION_FAILED,
            UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * Handle Missing ServletRequest Param Exception
     *
     * @param exception
     *            the exception
     * @param request
     *            the request
     * @return response entity
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
        @Nonnull MissingServletRequestParameterException exception, @Nonnull HttpHeaders headers,
        @Nonnull HttpStatusCode status, @Nonnull WebRequest request) {
        String message = String.format("Required parameter '%s' is not present", exception.getParameterName());
        return handleException(exception, message, ErrorType.REQUEST_VALIDATION_FAILED, BAD_REQUEST);
    }

    /**
     * Handle Exception
     *
     * @param exception
     *            the exception
     * @param request
     *            the request
     * @return response entity
     */
    private ResponseEntity<Object> handleException(Exception exception, String message, ErrorType errorType,
        HttpStatusCode status) {
        log.error("Exception occurred:", exception);
        return new ResponseEntity<>(getResponseBody(message, errorType, status), HttpHeaders.EMPTY, status);
    }

    /**
     * Get response
     *
     * @param message
     *            the message
     * @param error
     *            type the error type
     * @param status
     *            the status
     * @return response
     */
    private Map<String, Object> getResponseBody(String message, ErrorType errorType, HttpStatusCode status) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("message", message);
        body.put("errorType", errorType);
        body.put("status", status.value());
        return body;
    }

}
