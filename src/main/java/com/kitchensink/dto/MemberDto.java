package com.kitchensink.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * The Class MemberDto.
 *
 * @author prerna
 */
@Data
public class MemberDto {

    /** The id */
    private String id;

    /** The name */
    private String name;

    /** The email */
    private String email;

    /** The phone number */
    private String phoneNumber;

    /** The active */
    private boolean active;

    /** The blocked */
    private boolean blocked;

    /** The roles list */
    private List<String> roles;

    /** The joining date */
    private LocalDate joiningDate;
}
