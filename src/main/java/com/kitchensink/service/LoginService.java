package com.kitchensink.service;

import org.springframework.security.core.Authentication;

import com.kitchensink.dto.LoginRequestDto;

public interface LoginService {

    Authentication login(LoginRequestDto loginRequestDto);

}
