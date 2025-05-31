package com.kitchensink.service;

import org.springframework.security.core.Authentication;

import com.kitchensink.dto.RegisterMemberDto;

/**
 * The Interface MemberRegistrationService.
 */
public interface MemberRegistrationService {

    /**
     * Registers a new member after validation.
     *
     * @param newMember
     *            the new member data
     * @return Authentication object upon successful registration
     */
    Authentication register(RegisterMemberDto newMember);

}
