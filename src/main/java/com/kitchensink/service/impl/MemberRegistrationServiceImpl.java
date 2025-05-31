package com.kitchensink.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kitchensink.dto.RegisterMemberDto;
import com.kitchensink.entity.Member;
import com.kitchensink.enums.ErrorType;
import com.kitchensink.exception.AppAuthenticationException;
import com.kitchensink.exception.ConflictException;
import com.kitchensink.repository.MemberRepository;
import com.kitchensink.service.MemberRegistrationService;

import lombok.extern.slf4j.Slf4j;

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

    /** The rest template */
    @Autowired
    private RestTemplate restTemplate;

    /** The phone validation key */
    @Value("${phone.validation.apikey:123}")
    private String phoneValidationKey;

    /** The Constant PHONE_VALIDATION_URL */
    private static final String PHONE_VALIDATION_URL = "https://phonevalidation.abstractapi.com/v1/?api_key=";

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
        PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
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
        validatePhoneNumber(newMember.getPhoneNumber());

        Member member = Member.builder().name(newMember.getName()).email(newMember.getEmail()).active(true).phoneNumber(
            newMember.getPhoneNumber()).password(encryptPassword(newMember.getPassword())).roles(newMember.getRoles())
            .build();

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
     * Validate if phone number is valid
     *
     * @param phoneNumber
     *            the phone number
     */
    private void validatePhoneNumber(String phoneNumber) {
        if (!validatePhone(phoneNumber)) {
            throw new AppAuthenticationException("Invalid phone number: " + phoneNumber,
                ErrorType.PHONE_NUMBER_INVALID);
        }
    }

    /**
     * Call client to validate phone number
     *
     * @param phoneNumber
     *            the phone number
     * @return boolean
     */
    private boolean validatePhone(String phoneNumber) {
        try {
            String url = PHONE_VALIDATION_URL + phoneValidationKey + "&phone=" + phoneNumber;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
            return jsonNode.path("valid").asBoolean();
        } catch (Exception e) {
            log.warn("Phone validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Authenticat member
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
