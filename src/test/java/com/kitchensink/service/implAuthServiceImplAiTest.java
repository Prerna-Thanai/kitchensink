package com.kitchensink.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;

import com.kitchensink.entity.Member;
import com.kitchensink.repository.MemberRepository;

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
    void testLoadUserByUsername_UserFoundAndNotBlocked() {
        // Arrange
        String email = "test@example.com";
        String password = "password";
        Member member = new Member();
        member.setEmail(email);
        member.setPassword(password);
        member.setActive(true);
        member.setBlocked(false);
        member.setRoles(Collections.singletonList("ROLE_USER"));

        when(memberRepository.findByEmailAndActiveTrue(email)).thenReturn(Optional.of(member));

        // Act
        UserDetails userDetails = authService.loadUserByUsername(email);

        // Assert
        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals(password, userDetails.getPassword());
        assertFalse(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isEnabled());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        // Arrange
        String email = "notfound@example.com";
        when(memberRepository.findByEmailAndActiveTrue(email)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            authService.loadUserByUsername(email);
        });
        assertEquals("User with email " + email + " not found", exception.getMessage());
    }

    @Test
    void testLoadUserByUsername_UserBlocked() {
        // Arrange
        String email = "blocked@example.com";
        Member member = new Member();
        member.setEmail(email);
        member.setPassword("password");
        member.setActive(true);
        member.setBlocked(true);
        member.setRoles(Collections.singletonList("ROLE_USER"));

        when(memberRepository.findByEmailAndActiveTrue(email)).thenReturn(Optional.of(member));

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            authService.loadUserByUsername(email);
        });
        assertEquals("User with email " + email + " is blocked", exception.getMessage());
    }
}
