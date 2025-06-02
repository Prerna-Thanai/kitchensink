package com.kitchensink.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "Email should not be empty")
    @Email(message = "Email must be well formed")
    private String email;

    /** The password. */
    @NotBlank(message = "Password should not be empty")
    @ToString.Exclude
    private String password;

}
