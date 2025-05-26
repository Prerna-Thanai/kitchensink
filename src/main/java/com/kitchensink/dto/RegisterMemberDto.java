package com.kitchensink.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
	@NotNull
	@Size(min = 1, max = 30)
	@Pattern(regexp = "[^0-9]*", message = "Can only contains alphabets")
    private String name;

	/** The email. */
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email should be valid")
    @Pattern(
        regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
        message = "Email must match standard format"
    )
    private String email;

	/** The phone number. */
	@NotNull
	@Size(min = 10, max = 12)
	@Pattern(regexp = "^[6-9][0-9]{9}$", message = "Invalid mobile number")
    private String phoneNumber;
	
	/** The password. */
	@NotNull
	@Size(min = 7, max = 20)
	@ToString.Exclude
	private String password;

}
