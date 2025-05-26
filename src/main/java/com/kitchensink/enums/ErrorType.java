package com.kitchensink.enums;

public enum ErrorType {
    USER_NOT_FOUND("User not found"),
    INVALID_CREDENTIALS("Invalid credentials"),
    USER_ALREADY_EXISTS("User already exists"),
    EMAIL_ALREADY_REGISTERED("Email is already registered"),
    INVALID_EMAIL_FORMAT("Invalid email format"),
    PASSWORD_TOO_WEAK("Password is too weak"),
    ACCOUNT_LOCKED("Account is locked due to too many failed login attempts"),
    ACCESS_DENIED("Access denied"),
    INTERNAL_SERVER_ERROR("Internal server error"),
    SERVICE_UNAVAILABLE("Service is currently unavailable");

    private final String message;

    ErrorType(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
