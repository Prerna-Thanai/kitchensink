package com.kitchensink.dto;

import lombok.Data;

@Data
public class MemberSearchCriteria {

    /** The name */
    private String name;

    /** The email */
    private String email;

    /** The role */
    private String role;
}
