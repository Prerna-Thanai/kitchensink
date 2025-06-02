package com.kitchensink.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

/**
 * The Class LoginRequestDto.
 *
 * @author prerna
 */
@Data
public class LoginRequestDto {

    /** The email. */
    @NotNull(message = "Email should not be empty")
    @Email(message = "Email must be well formed")
    private String email;

    /** The password. */
    @NotNull(message = "Password should not be empty")
    @ToString.Exclude
    private String password;

}
