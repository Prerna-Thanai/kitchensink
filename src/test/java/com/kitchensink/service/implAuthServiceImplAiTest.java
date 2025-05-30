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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        String email = "nonexistent@example.com";
        when(memberRepository.findByEmailAndActiveTrue(email)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            authService.loadUserByUsername(email);
        });

        assertEquals("User with email " + email + " not found", exception.getMessage());
    }

    @Test
    void testLoadUserByUsername_UserIsBlocked() {
        String email = "blocked@example.com";
        Member blockedMember = new Member();
        blockedMember.setEmail(email);
        blockedMember.setBlocked(true);
        blockedMember.setActive(true);
        blockedMember.setPassword("password");
        blockedMember.setRoles(Collections.emptyList());

        when(memberRepository.findByEmailAndActiveTrue(email)).thenReturn(Optional.of(blockedMember));

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            authService.loadUserByUsername(email);
        });

        assertEquals("User with email " + email + " is blocked", exception.getMessage());
    }

    @Test
    void testLoadUserByUsername_UserIsActive() {
        String email = "active@example.com";
        Member activeMember = new Member();
        activeMember.setEmail(email);
        activeMember.setBlocked(false);
        activeMember.setActive(true);
        activeMember.setPassword("password");
        activeMember.setRoles(Collections.singletonList("ROLE_USER"));

        when(memberRepository.findByEmailAndActiveTrue(email)).thenReturn(Optional.of(activeMember));

        UserDetails userDetails = authService.loadUserByUsername(email);

        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertFalse(userDetails.isAccountNonLocked());
        assertFalse(userDetails.isCredentialsNonExpired());
        assertFalse(userDetails.isAccountNonExpired());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }
}
