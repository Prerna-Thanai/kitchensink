package com.kitchensink.entity;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * The Class Member.
 *
 * @author prerna
 */
@Document(collection = "member")
@Data
public class Member {

    /** The id */
    @Id
    private String id;

    /** The name */
    private String name;

    /** The email */
    @Indexed(unique = true)
    private String email;

    /** The phone number */
    private String phoneNumber;

    /** The password */
    @ToString.Exclude
    private String password;

    /** The active */
    private boolean active = true;

    /** The blocked */
    private boolean blocked = false;

    /** The failed login attempts */
    private int failedLoginAttempts;

    /** The blocked at */
    private LocalDateTime blockedAt;

    /** The roles list */
    private List<String> roles;

    /** The created at */
    @CreatedDate
    private LocalDateTime createdAt;

    /** The last updated at */
    @LastModifiedDate
    private LocalDateTime updatedAt;

}
