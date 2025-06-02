package com.kitchensink.service.impl;

import com.kitchensink.dto.LoginRequestDto;
import com.kitchensink.entity.Member;
import com.kitchensink.enums.ErrorType;
import com.kitchensink.exception.AppAuthenticationException;
import com.kitchensink.repository.MemberRepository;
import com.kitchensink.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * The Class LoginServiceImpl.
 *
 * @author prerna
 */
@Service
@Slf4j
public class LoginServiceImpl implements LoginService {

    /** The authentication manager */
    private final AuthenticationManager authenticationManager;

    /** The member repository */
    private final MemberRepository memberRepository;

    /**
     * LoginServiceImpl constructor
     *
     * @param authenticationManager
     *            the authentication manager
     * @param memberRepository
     *            the member repository
     */
    public LoginServiceImpl(AuthenticationManager authenticationManager, MemberRepository memberRepository) {
        this.authenticationManager = authenticationManager;
        this.memberRepository = memberRepository;
    }

    /**
     * Login member
     *
     * @param loginRequestDto
     *            the login request dto
     * @return authentication
     */
    @Override
    public Authentication login(LoginRequestDto loginRequestDto) {
        log.info("Logging in with email: {}", loginRequestDto.getEmail());
        Optional<Member> loggingMember = memberRepository.findByEmail(loginRequestDto.getEmail());
        if (loggingMember.isEmpty() || !loggingMember.get().isActive()) {
            log.error("Member with email {} doesn't exist", loginRequestDto.getEmail());
            throw new AppAuthenticationException("Member with email " + loginRequestDto.getEmail() + " doesn't exist",
                ErrorType.MEMBER_NOT_FOUND);
        } else if (loggingMember.get().isBlocked()) {
            log.error("Account blocked for member with email {}", loginRequestDto.getEmail());
            throw new AppAuthenticationException("Account blocked for member with email " + loginRequestDto.getEmail(),
                ErrorType.ACCOUNT_BLOCKED);
        }
        try {
            return authenticate(loginRequestDto.getEmail(), loginRequestDto.getPassword());
        } catch (BadCredentialsException e) {
            handleFailedLogin(loginRequestDto.getEmail());
            throw e;
        }
    }

    /**
     * Authenticate member
     *
     * @param username
     *            the username
     * @param password
     *            the password
     * @return authentication
     */
    private Authentication authenticate(String username, String password) {
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    /**
     * Handle member details when Failed Login
     *
     * @param email
     *            the email
     */
    private void handleFailedLogin(String email) {
        Optional<Member> memberOptional = memberRepository.findByEmail(email);
        if(memberOptional.isEmpty()){
            return;
        }
        Member member = memberOptional.get();
        int attempts = member.getFailedLoginAttempts() + 1;
        member.setFailedLoginAttempts(attempts);

        if (attempts >= 3) {
            member.setBlocked(true);
            member.setBlockedAt(LocalDateTime.now());
        }

        memberRepository.save(member);
    }

}
