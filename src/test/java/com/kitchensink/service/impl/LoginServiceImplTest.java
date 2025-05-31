package com.kitchensink.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.kitchensink.dto.LoginRequestDto;
import com.kitchensink.entity.Member;
import com.kitchensink.enums.ErrorType;
import com.kitchensink.exception.AppAuthenticationException;
import com.kitchensink.repository.MemberRepository;
import com.kitchensink.service.impl.LoginServiceImpl;

public class LoginServiceImplTest {

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    LoginServiceImpl loginService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoginSuccess() {
        String email = "user@example.com";
        String password = "password";

        Member member = new Member();
        member.setActive(true);
        member.setBlocked(false);
        member.setFailedLoginAttempts(0);

        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail(email);
        dto.setPassword(password);

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(
            authentication);

        Authentication result = loginService.login(dto);

        assertNotNull(result);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testLoginMemberNotFound() {
        String email = "notfound@example.com";
        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail(email);

        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        AppAuthenticationException ex = assertThrows(AppAuthenticationException.class, () -> loginService.login(dto));
        assertEquals(ErrorType.MEMBER_NOT_FOUND, ex.getErrorType());
    }

    @Test
    void testLoginMemberBlocked() {
        String email = "blocked@example.com";
        Member member = new Member();
        member.setActive(true);
        member.setBlocked(true);

        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail(email);

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));

        AppAuthenticationException ex = assertThrows(AppAuthenticationException.class, () -> loginService.login(dto));
        assertEquals(ErrorType.ACCOUNT_BLOCKED, ex.getErrorType());
    }

    @Test
    void testLoginBadCredentials_IncrementsFailedAttemptsAndBlocksIf3() {
        String email = "user@example.com";
        String password = "wrongpass";

        Member member = new Member();
        member.setActive(true);
        member.setBlocked(false);
        member.setFailedLoginAttempts(2); // simulate 2 previous failed attempts

        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail(email);
        dto.setPassword(password);

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(
            new BadCredentialsException("Bad credentials"));
        when(memberRepository.save(any(Member.class))).thenAnswer(i -> i.getArgument(0));

        BadCredentialsException ex = assertThrows(BadCredentialsException.class, () -> loginService.login(dto));

        assertTrue(member.isBlocked());
        assertEquals(3, member.getFailedLoginAttempts());
        assertNotNull(member.getBlockedAt());

        verify(memberRepository).save(member);
    }

    @Test
    void testLoginBadCredentials_IncrementsFailedAttemptsLessThan3() {
        String email = "user@example.com";
        String password = "wrongpass";

        Member member = new Member();
        member.setActive(true);
        member.setBlocked(false);
        member.setFailedLoginAttempts(1); // less than 2 previous attempts

        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail(email);
        dto.setPassword(password);

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(
            new BadCredentialsException("Bad credentials"));
        when(memberRepository.save(any(Member.class))).thenAnswer(i -> i.getArgument(0));

        BadCredentialsException ex = assertThrows(BadCredentialsException.class, () -> loginService.login(dto));

        assertFalse(member.isBlocked());
        assertEquals(2, member.getFailedLoginAttempts());
        assertNull(member.getBlockedAt());

        verify(memberRepository).save(member);
    }
}
