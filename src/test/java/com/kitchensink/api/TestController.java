package com.kitchensink.api;

import com.kitchensink.dto.LoginRequestDto;
import com.kitchensink.enums.ErrorType;
import com.kitchensink.exception.BaseApplicationException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController{
    @GetMapping("/base-exception")
    public void throwBaseException(){
        throw new BaseApplicationException("Base error", ErrorType.UNKNOWN, HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/bad-credentials")
    public void throwBadCredentials(){
        throw new BadCredentialsException("Bad credentials");
    }

    @GetMapping("/username-not-found")
    public void throwUsernameNotFound(){
        throw new UsernameNotFoundException("User not found");
    }

    @GetMapping("/uncaught")
    public void throwUncaughtException(){
        throw new RuntimeException("Unexpected error");
    }

    @PostMapping("/test-login")
    public void handleRequest(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        // This method is just to demonstrate handling of a request with validation.
        // If the request body is invalid, it will throw a MethodArgumentNotValidException.
        // The exception will be handled by the ExceptionAdvice class.
        if (loginRequestDto.getEmail() == null || loginRequestDto.getPassword() == null) {
            throw new BaseApplicationException("Email and password are required", ErrorType.REQUEST_VALIDATION_FAILED, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/test-param")
    public void handleRequest(@RequestParam String param) {
        // This method is just to demonstrate handling of a request with a query parameter.
        // If the parameter is missing, it will throw a MissingServletRequestParameterException.
        // The exception will be handled by the ExceptionAdvice class.
        if (param == null || param.isEmpty()) {
            throw new BaseApplicationException("Parameter is required", ErrorType.REQUEST_VALIDATION_FAILED, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/test-mismatch")
    public void handleRequest(@RequestParam Boolean param) {
        // This method is just to demonstrate handling of a request with a query parameter.
        // If the parameter is missing, it will throw a MissingServletRequestParameterException.
        // The exception will be handled by the ExceptionAdvice class.
        if (param == null ) {
            throw new BaseApplicationException("Parameter is required", ErrorType.REQUEST_VALIDATION_FAILED, HttpStatus.BAD_REQUEST);
        }
    }
}