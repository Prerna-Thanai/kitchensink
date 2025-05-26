package com.kitchensink.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
public class MemberDto{

    private String name;

    private String email;

    private String phoneNumber;
    private boolean isActive;

    private List<String> roles;
}
