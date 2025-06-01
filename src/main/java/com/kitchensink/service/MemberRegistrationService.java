package com.kitchensink.service;

import com.kitchensink.dto.RegisterMemberDto;
import org.springframework.security.core.Authentication;

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
