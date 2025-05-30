package com.kitchensink.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateMemberRequest {

    /** The name. */
    @NotNull
    @Size(min = 1, max = 30)
    @Pattern(regexp = "[^0-9]*", message = "Can only contains alphabets")
    private String name;

    /** The phone number. */
    @NotNull
    @Size(min = 10, max = 12)
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Invalid mobile number")
    private String phoneNumber;

    @NotEmpty(message = "At least one role must be added")
    @Size(min = 1, max = 10, message = "Max of 10 roles can be assigned")
    private List<String> roles;

    private boolean unBlockMember = false;

}
