package com.kitchensink.service.impl;

import java.util.Optional;

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
import com.kitchensink.exception.ConflictException;
import com.kitchensink.repository.MemberRepository;
import com.kitchensink.service.MemberRegistrationService;

import lombok.extern.slf4j.Slf4j;

/**
 * The Class MemberRegistrationServiceImpl.
 */
@Service
@Slf4j
public class MemberRegistrationServiceImpl implements MemberRegistrationService {

    private final AuthenticationManager authenticationManager;

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    private RestTemplate restTemplate;

    private final String phoneValidationUrl = "https://phonevalidation.abstractapi.com/v1/?api_key=";

    @Value("${phone.validation.apikey:123}")
    private String phoneValidationKey;

    public MemberRegistrationServiceImpl(AuthenticationManager authenticationManager, MemberRepository memberRepository,
        PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Register members.
     *
     * @param member
     *            the member
     * @return
     * @throws Exception
     *             the Exception
     */
    @Override
    public Authentication register(RegisterMemberDto newMember) {
        log.info("Registering {}", newMember.getEmail());
        // Check if member with same email or phone number already exists
        Optional<Member> existingMemberByEmailOptional = memberRepository.findByEmail(newMember.getEmail());
        if (existingMemberByEmailOptional.isPresent()) {
            log.error("Member with email {} already exists", newMember.getEmail());
            throw new ConflictException("Member with email " + newMember.getEmail() + " already exists",
                ErrorType.EMAIL_ALREADY_REGISTERED);
        }
        Member existingMemberByPhone = memberRepository.findByPhoneNumber(newMember.getPhoneNumber());
        if (existingMemberByPhone != null) {
            log.error("Member with phone number {} already exists", newMember.getPhoneNumber());
            throw new ConflictException("Member with phone number " + newMember.getPhoneNumber() + " already "
                + "exists", ErrorType.USER_ALREADY_EXISTS);
        }
        // disabling for now but this is working
        // if (!validatePhone(newMember.getPhoneNumber())) {
        // throw new AuthenticationException("Phone number " + newMember.getPhoneNumber() + " is invalid",
        // ErrorType.PHONE_NUMBER_INVALID);
        // }
        Member member = Member.builder().name(newMember.getName()).email(newMember.getEmail()).active(true).phoneNumber(
            newMember.getPhoneNumber()).password(encryptPassword(newMember.getPassword())).roles(newMember.getRoles())
            .build();
        memberRepository.insert(member);
        return authenticate(newMember.getEmail(), newMember.getPassword());
    }

    private boolean validatePhone(String phoneNumber) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(phoneValidationUrl + phoneValidationKey
                + "&phone=" + phoneNumber, String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            boolean isValid = jsonNode.path("valid").asBoolean();
            return isValid;
        } catch (Exception e) {
            return false;
        }
    }

    private Authentication authenticate(String username, String password) {
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    private String encryptPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

}
