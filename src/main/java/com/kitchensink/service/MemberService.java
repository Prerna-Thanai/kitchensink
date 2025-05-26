package com.kitchensink.service;

import com.kitchensink.dto.LoginRequestDto;
import com.kitchensink.dto.MemberDto;
import com.kitchensink.dto.RegisterMemberDto;
import org.springframework.security.core.Authentication;

public interface MemberService{

     Authentication login(LoginRequestDto loginRequestDto);

     Authentication register(RegisterMemberDto newMember);

     MemberDto currentUserData(Authentication authentication);

}
