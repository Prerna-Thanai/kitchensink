package com.kitchensink.service;

import org.springframework.security.core.Authentication;

import com.kitchensink.dto.RegisterMemberDto;

/**
 * The Interface MemberRegistrationService.
 */
public interface MemberRegistrationService {

    Authentication register(RegisterMemberDto newMember);

}
