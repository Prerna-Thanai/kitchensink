package com.kitchensink.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

@Data
public class LoginRequest {

    /** The email. */
    @NotNull
    @Email(message = "Email should not be empty")
    private String email;

    /** The password. */
    @NotNull(message = "Password should not be empty")
    @ToString.Exclude
    private String password;

}
