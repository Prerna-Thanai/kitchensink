package com.kitchensink.service.impl;

import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.kitchensink.dto.LoginRequestDto;
import com.kitchensink.entity.Member;
import com.kitchensink.enums.ErrorType;
import com.kitchensink.exception.AuthenticationException;
import com.kitchensink.repository.MemberRepository;
import com.kitchensink.service.LoginService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LoginServiceImpl implements LoginService {

    private final AuthenticationManager authenticationManager;

    private final MemberRepository memberRepository;

    public LoginServiceImpl(AuthenticationManager authenticationManager, MemberRepository memberRepository) {
        this.authenticationManager = authenticationManager;
        this.memberRepository = memberRepository;
    }

    @Override
    public Authentication login(LoginRequestDto loginRequestDto) {
        log.info("Logging in with email: {}", loginRequestDto.getEmail());
        Optional<Member> loggingUser = memberRepository.findByEmail(loginRequestDto.getEmail());
        if (loggingUser.isEmpty() || loggingUser.isPresent() && !loggingUser.get().isActive()) {
            log.error("Member with email {} doesn't exist", loginRequestDto.getEmail());
            throw new AuthenticationException("Member with email " + loginRequestDto.getEmail() + " doesn't exist",
                ErrorType.MEMBER_NOT_FOUND);
        } else if (loggingUser.isPresent() && loggingUser.get().isBlocked()) {
            log.error("Account blocked for member with email {}", loginRequestDto.getEmail());
            throw new AuthenticationException("Account blocked for member with email " + loginRequestDto.getEmail(),
                ErrorType.ACCOUNT_BLOCKED);
        }
        return authenticate(loginRequestDto.getEmail(), loginRequestDto.getPassword());
    }

    private Authentication authenticate(String username, String password) {
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

}
