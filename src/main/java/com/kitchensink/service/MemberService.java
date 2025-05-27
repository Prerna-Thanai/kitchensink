package com.kitchensink.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import com.kitchensink.dto.MemberDto;
import com.kitchensink.dto.UpdateMemberRequest;
import com.kitchensink.entity.Member;

public interface MemberService {

    MemberDto currentUserData(Authentication authentication);

    Page<Member> getAllMembers(Pageable pageable, boolean showInactiveMembers);

    void deleteMemberById(String memberId);

    Member updateMemberDetails(String memberId, UpdateMemberRequest updateRequest);

}
