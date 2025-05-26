package com.kitchensink.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "member")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Member {

    @Id
    private String id;

    private String name;

    @Indexed(unique = true)
    private String email;

    private String phoneNumber;

    @ToString.Exclude
    private String password;

    private boolean isActive;

    private List<String> roles;

    private LocalDateTime createdAt;

}
