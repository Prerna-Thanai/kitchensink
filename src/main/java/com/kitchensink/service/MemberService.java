package com.kitchensink.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import com.kitchensink.dto.MemberDto;
import com.kitchensink.dto.MemberSearchCriteria;
import com.kitchensink.dto.UpdateMemberRequest;

/**
 * The Interface MemberService.
 *
 * @author prerna
 */
public interface MemberService {

    /**
     * Get current user data
     *
     * @param authentication
     *            the authentication
     * @return member
     */
    MemberDto currentUserData(Authentication authentication);

    /**
     * Get all members
     *
     * @param pageable
     *            the pageable
     * @param showInactiveMembers
     *            the show inactive members
     * @return members
     */
    Page<MemberDto> getAllMembers(Pageable pageable, boolean showInactiveMembers);

    /**
     * Delete member by member id
     *
     * @param memberId
     *            the param member id
     * @param authentication
     */
    void deleteMemberById(String memberId, Authentication authentication);

    /**
     * Update member details
     *
     * @param memberId
     *            the member id
     * @param authentication
     * @param updateRequest
     *            the update request
     * @return member
     */
    MemberDto updateMemberDetails(String memberId, Authentication authentication, UpdateMemberRequest updateRequest);

    /**
     * Validate if phone number is valid
     *
     * @param phoneNumber
     *            the phone number
     */
    public void validatePhoneNumber(String phoneNumber);

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
    Page<MemberDto> getFilteredMembersByCriteria(Pageable pageable, boolean showInactiveMembers,
        MemberSearchCriteria searchCriteria);

}
