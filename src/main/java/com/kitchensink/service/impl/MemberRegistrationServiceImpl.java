package com.kitchensink.service.impl;

import com.kitchensink.enums.ErrorType;
import com.kitchensink.exception.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kitchensink.dto.RegisterMemberDto;
import com.kitchensink.entity.Member;
import com.kitchensink.repository.MemberRepository;
import com.kitchensink.service.MemberRegistrationService;
import com.kitchensink.service.MemberService;

/**
 * The Class MemberRegistrationServiceImpl.
 */
@Service
public class MemberRegistrationServiceImpl implements MemberRegistrationService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /** The password encoder. */
    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Logger log = LoggerFactory.getLogger(MemberService.class);

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
    public Member register(RegisterMemberDto newMember) throws Exception {

        log.info("Registering {}", newMember.getName());
        // Check if member with same email or phone number already exists
        Member existingMemberByEmail = memberRepository.findByEmail(newMember.getEmail());
        if (existingMemberByEmail != null) {
            log.error("Member with email {} already exists", newMember.getEmail());
            throw new AuthenticationException("Member with email " + newMember.getEmail() + " already exists", ErrorType.EMAIL_ALREADY_REGISTERED);
        }
        Member existingMemberByPhone = memberRepository.findByPhoneNumber(newMember.getPhoneNumber());
        if (existingMemberByPhone != null) {
            log.error("Member with phone number {} already exists", newMember.getPhoneNumber());
            throw new AuthenticationException("Member with phone number " + newMember.getPhoneNumber() + " already exists", ErrorType.USER_ALREADY_EXISTS);
        }
        Member member = Member.builder().name(newMember.getName()).email(newMember.getEmail()).phoneNumber(newMember
            .getPhoneNumber()).password(encryptPassword(newMember.getPassword())).build();
        Member registeredMember = memberRepository.save(member);
        if (registeredMember == null) {
            // throw exception
        }
        return registeredMember;

    }

    private String encryptPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

}
