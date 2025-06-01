package com.kitchensink.dto;

import com.kitchensink.validation.ValidUserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * The Class RegisterMemberDto.
 *
 * @author prerna
 */
@Data
public class RegisterMemberDto {

    /** The name. */
    @NotBlank(message = "Name must not be blank")
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

    /** The roles list */
    @NotEmpty(message = "At least one role must be added")
    @Size(min = 1, max = 1, message = "Max of 1 roles can be assigned")
    @ValidUserRole(message = "Member can only register as 'USER'")
    private List<String> roles;

}
