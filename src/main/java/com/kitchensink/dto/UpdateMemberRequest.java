package com.kitchensink.dto;

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

    private boolean isBlocked;

}
