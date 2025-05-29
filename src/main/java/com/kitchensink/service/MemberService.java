package com.kitchensink.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import com.kitchensink.dto.MemberDto;
import com.kitchensink.dto.UpdateMemberRequest;

public interface MemberService {

    MemberDto currentUserData(Authentication authentication);

    Page<MemberDto> getAllMembers(Pageable pageable, boolean showInactiveMembers);

    void deleteMemberById(String memberId);

    MemberDto updateMemberDetails(String memberId, UpdateMemberRequest updateRequest);

}
