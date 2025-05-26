package com.kitchensink.enums;

import lombok.Getter;

@Getter
public enum ErrorType {
    USER_ALREADY_EXISTS("User already exists"),
    EMAIL_ALREADY_REGISTERED("Email is already registered"),
    TOKEN_EXPIRED("Token has expired"),
    TOKEN_INVALID("Token is invalid"),
    TOKEN_NOT_FOUND("Token not found"),
    MEMBER_NOT_AUTHENTICATED("Member is not authenticated");

    private final String message;

    ErrorType(String message) {
        this.message = message;
    }

}
