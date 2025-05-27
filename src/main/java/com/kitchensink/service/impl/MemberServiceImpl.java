package com.kitchensink.service.impl;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kitchensink.dto.MemberDto;
import com.kitchensink.dto.UpdateMemberRequest;
import com.kitchensink.entity.Member;
import com.kitchensink.enums.ErrorType;
import com.kitchensink.exception.AuthenticationException;
import com.kitchensink.repository.MemberRepository;
import com.kitchensink.service.MemberService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MemberServiceImpl implements MemberService {

    private final AuthenticationManager authenticationManager;

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    public MemberServiceImpl(AuthenticationManager authenticationManager, MemberRepository memberRepository,
        PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // @Override
    // public Authentication register(RegisterMemberDto newMember) {
    // log.info("Registering {}", newMember.getEmail());
    // // Check if member with same email or phone number already exists
    // Member existingMemberByEmail = memberRepository.findByEmail(newMember.getEmail());
    // if (existingMemberByEmail != null) {
    // log.error("Member with email {} already exists", newMember.getEmail());
    // throw new com.kitchensink.exception.AuthenticationException("Member with email " + newMember.getEmail()
    // + " already exists", ErrorType.EMAIL_ALREADY_REGISTERED);
    // }
    // Member existingMemberByPhone = memberRepository.findByPhoneNumber(newMember.getPhoneNumber());
    // if (existingMemberByPhone != null) {
    // log.error("Member with phone number {} already exists", newMember.getPhoneNumber());
    // throw new AuthenticationException("Member with phone number " + newMember.getPhoneNumber() + " already "
    // + "exists", ErrorType.USER_ALREADY_EXISTS);
    // }
    // Member member = Member.builder().name(newMember.getName()).email(newMember.getEmail()).isActive(true)
    // .phoneNumber(newMember.getPhoneNumber()).password(encryptPassword(newMember.getPassword())).roles(newMember
    // .getRoles()).build();
    // memberRepository.insert(member);
    // return authenticate(newMember.getEmail(), newMember.getPassword());
    // }

    @Override
    public MemberDto currentUserData(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("Member not authenticated or session expired");
            throw new AuthenticationException("Member not authenticated or session expired",
                ErrorType.MEMBER_NOT_AUTHENTICATED);
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Member member = memberRepository.findByEmail(userDetails.getUsername());
        return MemberDto.builder().name(member.getName()).email(member.getEmail()).isActive(member.isActive())
            .phoneNumber(member.getPhoneNumber()).roles(member.getRoles()).build();
    }

    // private Authentication authenticate(String username, String password) {
    // return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    // }
    //
    // private String encryptPassword(String rawPassword) {
    // return passwordEncoder.encode(rawPassword);
    // }

    @Override
    public Page<Member> getAllMembers(Pageable pageable, boolean showInactiveMembers) {
        if (showInactiveMembers) {
            return memberRepository.findAll(pageable); // Show all users
        } else {
            return memberRepository.findByIsActiveTrue(pageable); // Only active users
        }
    }

    @Override
    public void deleteMemberById(String memberId) {
        // TODO update code to get data based on active/inactive

        // soft delete
        Optional<Member> memberOptional = memberRepository.findById(memberId);
        if (memberOptional.isEmpty()) {
            // throw new RegisterUserException();
            // TODO throw exception;
        }
        memberOptional.get().setActive(false);
        memberRepository.save(memberOptional.get());

    }

    @Override
    public Member updateMemberDetails(String memberId, UpdateMemberRequest updateRequest) {
        // TODO update to throw specific exception
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new RuntimeException(
            "Member not found with id: " + memberId));

        // Only update allowed fields
        member.setName(updateRequest.getName());
        member.setPhoneNumber(updateRequest.getPhoneNumber());
        member.setBlocked(updateRequest.isBlocked());

        return memberRepository.save(member);

    }

}
