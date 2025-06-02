package com.kitchensink.enums;

import lombok.Getter;

/**
 * The Class ErrorType.
 *
 * @author prerna
 */
@Getter
public enum ErrorType{
    USER_ALREADY_EXISTS,
    EMAIL_ALREADY_REGISTERED,
    TOKEN_EXPIRED,
    TOKEN_INVALID,
    TOKEN_NOT_FOUND,
    MEMBER_NOT_AUTHENTICATED,
    MEMBER_NOT_AUTHORISED,
    MEMBER_NOT_FOUND,
    ACCOUNT_BLOCKED,
    PHONE_NUMBER_INVALID,
    INVALID_CREDENTIALS,
    NOT_FOUND,
    REQUEST_VALIDATION_FAILED,
    UNKNOWN


}
