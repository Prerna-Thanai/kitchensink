package com.kitchensink.service.impl;

import com.kitchensink.dto.RegisterMemberDto;
import com.kitchensink.entity.Member;
import com.kitchensink.enums.ErrorType;
import com.kitchensink.exception.ConflictException;
import com.kitchensink.repository.MemberRepository;
import com.kitchensink.service.MemberRegistrationService;
import com.kitchensink.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * The Class MemberRegistrationServiceImpl.
 *
 * @author prerna
 */
@Service
@Slf4j
public class MemberRegistrationServiceImpl implements MemberRegistrationService {

    /** The authentication manager */
    private final AuthenticationManager authenticationManager;

    /** The member repository */
    private final MemberRepository memberRepository;

    /** The password encoder */
    private final PasswordEncoder passwordEncoder;

/** The member service */
    private final MemberService memberService;

    /**
     * MemberRegistrationServiceImpl constructor
     *
     * @param authenticationManager
     *            the authentication manager
     * @param memberRepository
     *            the member repository
     * @param passwordEncoder
     *            the password encoder
     */
    public MemberRegistrationServiceImpl(AuthenticationManager authenticationManager, MemberRepository memberRepository,
        PasswordEncoder passwordEncoder, MemberService memberService) {
        this.authenticationManager = authenticationManager;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.memberService = memberService;
    }

    /**
     * Registers a new member after validation.
     *
     * @param newMember
     *            the new member data
     * @return Authentication object upon successful registration
     */
    @Override
    public Authentication register(RegisterMemberDto newMember) {
        log.info("Registering member: {}", newMember.getEmail());

        validateUniqueness(newMember);
        memberService.validatePhoneNumber(newMember.getPhoneNumber());

        Member member = new Member();
        member.setName(newMember.getName());
        member.setEmail(newMember.getEmail());
        member.setPhoneNumber(newMember.getPhoneNumber());
        member.setPassword(encryptPassword(newMember.getPassword()));
        member.setActive(true);
        member.setBlocked(false);
        member.setRoles(new ArrayList<>(newMember.getRoles()));

        memberRepository.insert(member);

        return authenticate(newMember.getEmail(), newMember.getPassword());
    }

    /**
     * Validate uniqueness of new member details
     *
     * @param newMember
     *            the new member
     */
    private void validateUniqueness(RegisterMemberDto newMember) {
        memberRepository.findByEmail(newMember.getEmail()).ifPresent(existing -> {
            log.error("Email already registered: {}", newMember.getEmail());
            throw new ConflictException("Email already registered: " + newMember.getEmail(),
                ErrorType.EMAIL_ALREADY_REGISTERED);
        });

        Member existingByPhone = memberRepository.findByPhoneNumber(newMember.getPhoneNumber());
        if (existingByPhone != null) {
            log.error("Phone number already registered: {}", newMember.getPhoneNumber());
            throw new ConflictException("Phone number already registered: " + newMember.getPhoneNumber(),
                ErrorType.USER_ALREADY_EXISTS);
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
     * Encrypt Password
     *
     * @param rawPassword
     *            the raw password
     * @return encrypted password
     */
    private String encryptPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
