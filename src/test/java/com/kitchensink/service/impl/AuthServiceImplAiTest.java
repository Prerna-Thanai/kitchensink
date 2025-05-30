package com.kitchensink.service.impl;

import com.kitchensink.entity.Member;
import com.kitchensink.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    private Member member;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        member = new Member();
        member.setEmail("test@example.com");
        member.setPassword("password");
        member.setActive(true);
        member.setBlocked(false);
        member.setRoles(Collections.singletonList("ROLE_USER"));
    }

    @Test
    void testLoadUserByUsername_UserFound() {
        when(memberRepository.findByEmailAndActiveTrue("test@example.com")).thenReturn(Optional.of(member));

        UserDetails userDetails = authService.loadUserByUsername("test@example.com");

        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(userDetails.isEnabled());
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        when(memberRepository.findByEmailAndActiveTrue("unknown@example.com")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            authService.loadUserByUsername("unknown@example.com");
        });

        assertEquals("User with email unknown@example.com not found", exception.getMessage());
    }

    @Test
    void testLoadUserByUsername_UserBlocked() {
        member.setBlocked(true);
        when(memberRepository.findByEmailAndActiveTrue("test@example.com")).thenReturn(Optional.of(member));

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            authService.loadUserByUsername("test@example.com");
        });

        assertEquals("User with email test@example.com is blocked", exception.getMessage());
    }

    @Test
    void testLoadUserByUsername_UserInactive() {
        member.setActive(false);
        when(memberRepository.findByEmailAndActiveTrue("test@example.com")).thenReturn(Optional.of(member));

        UserDetails userDetails = authService.loadUserByUsername("test@example.com");

        assertNotNull(userDetails);
        assertFalse(userDetails.isEnabled());
    }
}
