package com.kitchensink.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Indexed;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Document(collection = "member")
@Builder
@Data
@AllArgsConstructor
public class Member implements Serializable {

    @Id
    private String id;

    @NotNull
    private String name;

    @NotNull
    private String email;

    @NotNull
    private String phoneNumber;
    
    @NotNull
    @JsonIgnore
    private String password;
    
    private LocalDateTime createdAt;

}
