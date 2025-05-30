package com.kitchensink.service.impl;

import com.kitchensink.entity.Member;
import com.kitchensink.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
    void loadUserByUsername_UserNotFound() {
        // Arrange
        String email = "test@example.com";
        when(memberRepository.findByEmailAndActiveTrue(email)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            authService.loadUserByUsername(email);
        });
        assertEquals("User with email " + email + " not found", exception.getMessage());
    }

    @Test
    void loadUserByUsername_UserBlocked() {
        // Arrange
        String email = "blocked@example.com";
        Member blockedMember = new Member();
        blockedMember.setEmail(email);
        blockedMember.setBlocked(true);
        blockedMember.setActive(true);
        when(memberRepository.findByEmailAndActiveTrue(email)).thenReturn(Optional.of(blockedMember));

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            authService.loadUserByUsername(email);
        });
        assertEquals("User with email " + email + " is blocked", exception.getMessage());
    }

    @Test
    void loadUserByUsername_UserFoundAndActive() {
        // Arrange
        String email = "active@example.com";
        Member activeMember = new Member();
        activeMember.setEmail(email);
        activeMember.setPassword("password");
        activeMember.setBlocked(false);
        activeMember.setActive(true);
        activeMember.setRoles(Collections.singletonList("ROLE_USER"));
        when(memberRepository.findByEmailAndActiveTrue(email)).thenReturn(Optional.of(activeMember));

        // Act
        UserDetails userDetails = authService.loadUserByUsername(email);

        // Assert
        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertFalse(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }
}
