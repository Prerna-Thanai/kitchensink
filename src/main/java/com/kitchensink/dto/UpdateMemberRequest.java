package com.kitchensink.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * The Class UpdateMemberRequest.
 *
 * @author prerna
 */
@Data
public class UpdateMemberRequest {

    /** The name. */
    @NotNull(message = "Name must not be null")
    @Size(min = 1, max = 30)
    @Pattern(regexp = "[^0-9]*", message = "Can only contains alphabets")
    private String name;

    /** The phone number. */
    @NotNull(message = "Phone number is required.")
    @Size(min = 10, max = 12)
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Invalid mobile number")
    private String phoneNumber;

    /** The roles list */
    @NotEmpty(message = "At least one role must be added")
    @Size(min = 1, max = 10, message = "Max of 10 roles can be assigned")
    private List<String> roles;

    /** The unblock member */
    private boolean unBlockMember = false;

}
