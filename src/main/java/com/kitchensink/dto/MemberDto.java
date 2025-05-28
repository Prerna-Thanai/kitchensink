package com.kitchensink.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class MemberDto {

    private String name;

    private String email;

    private String phoneNumber;
    private boolean isActive;
    private boolean isBlocked;

    private List<String> roles;
    private LocalDate joiningDate;
}
