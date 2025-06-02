package com.kitchensink.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kitchensink.enums.ErrorType;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.PropertyAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

/**
 * The Class ExceptionAdvice.
 *
 * @author prerna
 */
@ControllerAdvice
@Slf4j
public class ExceptionAdvice implements AuthenticationEntryPoint{


    private final ObjectMapper objectMapper;

    /**
     * Exception advice constructor
     */
    public ExceptionAdvice(ObjectMapper objectMapper) {
        super();
        this.objectMapper = objectMapper;
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
                                                               @Nonnull WebRequest request) {
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
        @Nonnull HttpMediaTypeNotSupportedException exception, @Nonnull WebRequest request) {
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
        @Nonnull HttpRequestMethodNotSupportedException exception, @Nonnull WebRequest request) {
        return handleException(exception, "Request Type not supported", ErrorType.REQUEST_VALIDATION_FAILED,
            METHOD_NOT_ALLOWED);
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
        @Nonnull MissingServletRequestParameterException exception, @Nonnull WebRequest request) {
        String message = String.format("Required parameter '%s' is not present", exception.getParameterName());
        return handleException(exception, message, ErrorType.REQUEST_VALIDATION_FAILED, BAD_REQUEST);
    }

    /**
     * Handle Missing Request Value Exception
     *
     * @param exception
     *            the exception
     * @param request
     *            the request
     * @return response entity
     */
    @ExceptionHandler(PropertyAccessException.class)
    protected ResponseEntity<Object> handleMissingRequestValueException(
            @Nonnull PropertyAccessException exception, @Nonnull WebRequest request) {
        String message = String.format("Invalid %s value: %s", exception.getPropertyName(), exception.getValue());
        return handleException(exception, message, ErrorType.REQUEST_VALIDATION_FAILED, BAD_REQUEST);
    }

    /**
     * Handle Missing Request Value Exception
     *
     * @param exception
     *            the exception
     * @param request
     *            the request
     * @return response entity
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    protected ResponseEntity<Object> handleAuthorizationDeniedException(
            @Nonnull AuthorizationDeniedException exception, @Nonnull WebRequest request) {
        return handleException(exception, "Access Denied", ErrorType.MEMBER_NOT_AUTHORISED, FORBIDDEN);
    }

    /**
     * Handle Exception
     *
     * @param exception
     *            the exception
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
     * @param errorType
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

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        ResponseEntity<Object> unauthorisedAccess = handleException(exception, "Access Denied", ErrorType.MEMBER_NOT_AUTHORISED, FORBIDDEN);
        response.setStatus(unauthorisedAccess.getStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().println(objectMapper.writeValueAsString(unauthorisedAccess.getBody()));
    }
}
