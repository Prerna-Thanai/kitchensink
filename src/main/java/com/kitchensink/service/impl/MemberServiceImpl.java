package com.kitchensink.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import com.kitchensink.exception.AppAuthenticationException;
import com.kitchensink.repository.MemberRepository;
import com.kitchensink.service.MemberService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public MemberDto currentUserData(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("Member not authenticated or session expired");
            throw new AppAuthenticationException("Member not authenticated or session expired",
                ErrorType.MEMBER_NOT_AUTHENTICATED);
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<Member> memberOptional = memberRepository.findByEmail(userDetails.getUsername());

        if (memberOptional.isEmpty()) {
            throw new AppAuthenticationException("Member not found", ErrorType.MEMBER_NOT_FOUND);
        }
        Member member = memberOptional.get();
        return MemberDto.builder().name(member.getName()).email(member.getEmail()).active(member.isActive()).blocked(
            member.isBlocked()).phoneNumber(member.getPhoneNumber()).roles(member.getRoles()).joiningDate(member
                .getCreatedAt().toLocalDate()).build();
    }


    @Override
    public Page<MemberDto> getAllMembers(Pageable pageable, boolean showInactiveMembers) {
        if (showInactiveMembers) {
            return transformMember(memberRepository.findAll(pageable)); // Show all users
        } else {
            return transformMember(memberRepository.findByActiveTrue(pageable)); // Only active users
        }
    }

    private Page<MemberDto> transformMember(Page<Member> membersPage) {

        List<MemberDto> memberDTOs = membersPage.getContent().stream().map((Member member) -> MemberDto.builder().id(
            member.getId()).name(member.getName()).email(member.getEmail()).phoneNumber(member.getPhoneNumber()).roles(
                member.getRoles()).joiningDate(member.getCreatedAt().toLocalDate()).active(member.isActive()).blocked(
                    member.isBlocked()).build()).collect(Collectors.toList());

        // Create a new PageImpl with the converted content and original pagination info
        return new PageImpl<>(memberDTOs, membersPage.getPageable(), membersPage.getTotalElements());
    }

    @Override
    public void deleteMemberById(String memberId) {
        // soft delete
        Optional<Member> memberOptional = memberRepository.findById(memberId);
        if (memberOptional.isEmpty()) {
            log.error("Member with memberId {} doesn't exist", memberId);
            throw new AppAuthenticationException("Member with memberId " + memberId + "doesn't exist",
                ErrorType.MEMBER_NOT_FOUND);
        }
        memberOptional.get().setActive(false);
        memberRepository.save(memberOptional.get());

    }

    @Override
    public MemberDto updateMemberDetails(String memberId, UpdateMemberRequest updateRequest) {
        Optional<Member> memberOptional = memberRepository.findById(memberId);
        if (memberOptional.isEmpty()) {
            log.error("Member with memberId {} doesn't exist", memberId);
            throw new AppAuthenticationException("Member with memberId " + memberId + "doesn't exist",
                ErrorType.MEMBER_NOT_FOUND);
        }

        // Only update allowed fields
        Member member = memberOptional.get();
        member.setName(updateRequest.getName());
        member.setPhoneNumber(updateRequest.getPhoneNumber());
        member.setRoles(updateRequest.getRoles());

        if (updateRequest.isUnBlockMember()) {
            member.setBlocked(false);
            member.setFailedLoginAttempts(0);
            member.setBlockedAt(null);
        }

        Member savedMember = memberRepository.save(member);
        return MemberDto.builder().id(member.getId()).name(savedMember.getName()).email(savedMember.getEmail())
            .phoneNumber(savedMember.getPhoneNumber()).roles(savedMember.getRoles()).joiningDate(savedMember
                .getCreatedAt().toLocalDate()).active(savedMember.isActive()).blocked(savedMember.isBlocked()).build();

    }

}
