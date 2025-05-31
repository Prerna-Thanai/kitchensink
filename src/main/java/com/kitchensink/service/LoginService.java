package com.kitchensink.service;

import org.springframework.security.core.Authentication;

import com.kitchensink.dto.LoginRequestDto;

/**
 * The Interface LoginService.
 *
 * @author prerna
 */
public interface LoginService {

    /**
     * Login member
     *
     * @param loginRequestDto
     *            the login request dto
     * @return authentication
     */
    Authentication login(LoginRequestDto loginRequestDto);

}
