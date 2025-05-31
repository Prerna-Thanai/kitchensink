package com.kitchensink.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberDto {

    private String id;

    private String name;

    private String email;

    private String phoneNumber;
    private boolean active;
    private boolean blocked;

    private List<String> roles;
    private LocalDate joiningDate;
}
