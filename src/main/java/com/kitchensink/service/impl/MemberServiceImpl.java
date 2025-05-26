package com.kitchensink.service.impl;

import com.kitchensink.dto.LoginRequestDto;
import com.kitchensink.dto.MemberDto;
import com.kitchensink.dto.RegisterMemberDto;
import com.kitchensink.entity.Member;
import com.kitchensink.enums.ErrorType;
import com.kitchensink.exception.AuthenticationException;
import com.kitchensink.repository.MemberRepository;
import com.kitchensink.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MemberServiceImpl implements MemberService{

    private final AuthenticationManager authenticationManager;

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    public MemberServiceImpl(AuthenticationManager authenticationManager, MemberRepository memberRepository,
                             PasswordEncoder passwordEncoder){
        this.authenticationManager = authenticationManager;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public Authentication login(LoginRequestDto loginRequestDto){
        log.info("Logging in with email: {}", loginRequestDto.getEmail());
        return authenticate(loginRequestDto.getEmail(), loginRequestDto.getPassword());
    }

    public Authentication register(RegisterMemberDto newMember){
        log.info("Registering {}", newMember.getEmail());
        // Check if member with same email or phone number already exists
        Member existingMemberByEmail = memberRepository.findByEmail(newMember.getEmail());
        if(existingMemberByEmail != null){
            log.error("Member with email {} already exists", newMember.getEmail());
            throw new com.kitchensink.exception.AuthenticationException("Member with email " + newMember.getEmail() + " already exists", ErrorType.EMAIL_ALREADY_REGISTERED);
        }
        Member existingMemberByPhone = memberRepository.findByPhoneNumber(newMember.getPhoneNumber());
        if(existingMemberByPhone != null){
            log.error("Member with phone number {} already exists", newMember.getPhoneNumber());
            throw new AuthenticationException("Member with phone number " + newMember.getPhoneNumber() + " already " +
                    "exists", ErrorType.USER_ALREADY_EXISTS);
        }
        Member member = Member.builder().name(newMember.getName())
                              .email(newMember.getEmail())
                              .isActive(true)
                              .phoneNumber(newMember.getPhoneNumber())
                              .password(encryptPassword(newMember.getPassword()))
                              .roles(newMember.getRoles())
                              .build();
        memberRepository.insert(member);
        return authenticate(newMember.getEmail(), newMember.getPassword());
    }

    @Override
    public MemberDto currentUserData(Authentication authentication){
        if(authentication == null || !authentication.isAuthenticated()){
            log.error("Member not authenticated or session expired");
            throw new AuthenticationException("Member not authenticated or session expired",
                    ErrorType.MEMBER_NOT_AUTHENTICATED);
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Member member = memberRepository.findByEmail(userDetails.getUsername());
        return MemberDto.builder()
                .name(member.getName())
                .email(member.getEmail())
                .isActive(member.isActive())
                .phoneNumber(member.getPhoneNumber())
                .roles(member.getRoles())
                .build();
    }

    private Authentication authenticate(String username, String password){
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
    }

    private String encryptPassword(String rawPassword){
        return passwordEncoder.encode(rawPassword);
    }

}
