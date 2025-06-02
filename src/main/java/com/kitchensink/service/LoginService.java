package com.kitchensink.service;

import com.kitchensink.dto.LoginRequestDto;
import org.springframework.security.core.Authentication;

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
