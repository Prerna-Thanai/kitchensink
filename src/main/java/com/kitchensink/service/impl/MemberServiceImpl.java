package com.kitchensink.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kitchensink.dto.MemberDto;
import com.kitchensink.dto.MemberSearchCriteria;
import com.kitchensink.dto.UpdateMemberRequest;
import com.kitchensink.entity.Member;
import com.kitchensink.enums.ErrorType;
import com.kitchensink.exception.AppAuthenticationException;
import com.kitchensink.repository.MemberRepository;
import com.kitchensink.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The Class MemberServiceImpl.
 *
 * @author prerna
 */
@Service
@Slf4j
public class MemberServiceImpl implements MemberService {

    /** The member repository */
    private final MemberRepository memberRepository;

    /** The rest template */
    private final RestTemplate restTemplate;

    private final boolean phoneValidationEnabled;
    /** The mongo template. */
    private final MongoTemplate mongoTemplate;

    /** The phone validation key */
    private final String phoneValidationKey;

    /** The Constant PHONE_VALIDATION_URL */
    private static final String PHONE_VALIDATION_URL = "https://phonevalidation.abstractapi.com/v1/?api_key=";

    /**
     * MemberServiceImpl constructor
     *
     * @param memberRepository
     *            the member repository
     */
    public MemberServiceImpl(MemberRepository memberRepository, RestTemplate restTemplate,
                             MongoTemplate mongoTemplate,
                             @Value("${phone.validation.enabled:true}") boolean phoneValidationEnabled,
                             @Value("${phone.validation.apikey:123}") String phoneValidationKey) {
        this.memberRepository = memberRepository;
        this.restTemplate = restTemplate;
        this.mongoTemplate = mongoTemplate;
        this.phoneValidationEnabled = phoneValidationEnabled;
        this.phoneValidationKey = phoneValidationKey;
    }

    /**
     * Get current user data
     *
     * @param authentication
     *            the authentication
     * @return member
     */
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
        return toMemberDto(member);
    }

    /**
     * Get all members
     *
     * @param pageable
     *            the pageable
     * @param showInactiveMembers
     *            the show inactive members
     * @return members
     */
    @Override
    public Page<MemberDto> getAllMembers(Pageable pageable, boolean showInactiveMembers) {
        if (showInactiveMembers) {
            return transformMember(memberRepository.findAll(pageable)); // Show all users
        } else {
            return transformMember(memberRepository.findByActiveTrue(pageable)); // Only active users
        }
    }

    /**
     * Transform member entity to dto
     *
     * @param membersPage
     *            the members page
     * @return member dto
     */
    private Page<MemberDto> transformMember(Page<Member> membersPage) {

        List<MemberDto> memberDTOs = membersPage.getContent().stream().map(this::toMemberDto).collect(Collectors.toList());

        // Create a new PageImpl with the converted content and original pagination info
        return new PageImpl<>(memberDTOs, membersPage.getPageable(), membersPage.getTotalElements());
    }

    private MemberDto toMemberDto(Member member){
        MemberDto memberDto = new MemberDto();
        memberDto.setId(member.getId());
        memberDto.setName(member.getName());
        memberDto.setEmail(member.getEmail());
        memberDto.setPhoneNumber(member.getPhoneNumber());
        memberDto.setRoles(member.getRoles());
        memberDto.setJoiningDate(member.getCreatedAt().toLocalDate());
        memberDto.setActive(member.isActive());
        memberDto.setBlocked(member.isBlocked());
        memberDto.setRoles(new ArrayList<>(member.getRoles()));
        return memberDto;
    }

    /**
     * Delete member by member id
     *
     * @param memberId
     *            the param member id
     */
    @Override
    public void deleteMemberById(String memberId) {
        // soft delete
        Optional<Member> memberOptional = memberRepository.findById(memberId);
        if (memberOptional.isEmpty()) {
            log.error("Member with memberId {} doesn't exist", memberId);
            throw new AppAuthenticationException("Member with memberId " + memberId + " doesn't exist",
                ErrorType.MEMBER_NOT_FOUND);
        }
        memberOptional.get().setActive(false);
        memberRepository.save(memberOptional.get());

    }

    /**
     * Update member details
     *
     * @param memberId
     *            the member id
     * @param updateRequest
     *            the update request
     * @return member
     */
    @Override
    public MemberDto updateMemberDetails(String memberId, UpdateMemberRequest updateRequest) {
        Optional<Member> memberOptional = memberRepository.findById(memberId);
        if (memberOptional.isEmpty()) {
            log.error("Member with memberId {} doesn't exist", memberId);
            throw new AppAuthenticationException("Member with memberId " + memberId + " doesn't exist",
                ErrorType.MEMBER_NOT_FOUND);
        }

        if (!memberOptional.get().getPhoneNumber().equals(updateRequest.getPhoneNumber())) {
            validatePhoneNumber(updateRequest.getPhoneNumber());
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
        return toMemberDto(savedMember);

    }

    /**
     * Validate if phone number is valid
     *
     * @param phoneNumber
     *            the phone number
     */
    @Override
    public void validatePhoneNumber(String phoneNumber) {
        if (phoneValidationEnabled && !validatePhone(phoneNumber)) {
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
     * Get filtered members
     *
     * @param pageable
     *            the pageable
     * @param showInactiveMembers
     *            the show inactive members
     * @param searchCriteria
     *            the search criteria
     * @return members
     */
    @Override
    public Page<MemberDto> getFilteredMembersByCriteria(Pageable pageable, boolean showInactiveMembers,
        MemberSearchCriteria searchCriteria) {

        List<MemberDto> filteredMembers = new ArrayList<>();
        Query query = new Query().with(pageable);

        // OR part: (name OR email)
        List<Criteria> orCriteria = new ArrayList<>();
        if (searchCriteria.getName() != null && !searchCriteria.getName().isEmpty()) {
            Pattern namePattern = Pattern.compile(".*" + Pattern.quote(searchCriteria.getName()) + ".*",
                Pattern.CASE_INSENSITIVE);
            orCriteria.add(Criteria.where("name").regex(namePattern));
        }

        if (searchCriteria.getEmail() != null && !searchCriteria.getEmail().isEmpty()) {
            Pattern emailPattern = Pattern.compile(".*" + Pattern.quote(searchCriteria.getEmail()) + ".*",
                Pattern.CASE_INSENSITIVE);
            orCriteria.add(Criteria.where("email").regex(emailPattern));
        }

        if (!orCriteria.isEmpty()) {
            query.addCriteria(new Criteria().orOperator(orCriteria.toArray(new Criteria[0])));
        }

        if (searchCriteria.getRole() != null && !searchCriteria.getRole().isEmpty()) {
            query.addCriteria(Criteria.where("roles").in(searchCriteria.getRole()));
        }

        List<Member> members = mongoTemplate.find(query, Member.class);
        members.forEach(mem -> filteredMembers.add(toMemberDto(mem)));

        long count = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), Member.class);

        return new PageImpl<>(filteredMembers, pageable, count);
    }

}
