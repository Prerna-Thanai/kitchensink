package com.kitchensink.dto;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 * The Class RegisterMember.
 *
 * @author prerna
 */
@Data
@AllArgsConstructor
public class RegisterMemberDto {

    /** The name. */
    @NotNull(message = "Name must not be null")
    @Size(min = 1, max = 30)
    @Pattern(regexp = "[^0-9]*", message = "Name can only contains alphabets")
    private String name;

    /** The email. */
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email should be valid")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Email must match standard format")
    private String email;

    /** The phone number. */
    @NotNull(message = "Phone number is required.")
    @Size(min = 10, max = 12)
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Invalid mobile number")
    private String phoneNumber;

    /** The password. */
    @NotNull(message = "Password is required.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
        message = "Password must be 8–20 characters long, and include uppercase, lowercase, number, and special character.")
    @ToString.Exclude
    private String password;

    @NotEmpty(message = "At least one role must be added")
    @Size(min = 1, max = 10, message = "Max of 10 roles can be assigned")
    private List<String> roles;

}
