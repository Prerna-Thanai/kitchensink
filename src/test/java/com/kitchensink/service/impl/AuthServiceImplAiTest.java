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
    void loadUserByUsername_UserFoundAndNotBlocked() {
        // Arrange
        String email = "test@example.com";
        Member member = new Member();
        member.setEmail(email);
        member.setPassword("password");
        member.setActive(true);
        member.setBlocked(false);
        member.setRoles(Collections.singletonList("USER"));

        when(memberRepository.findByEmailAndActiveTrue(email)).thenReturn(Optional.of(member));

        // Act
        UserDetails userDetails = authService.loadUserByUsername(email);

        // Assert
        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(userDetails.isEnabled());
    }

    @Test
    void loadUserByUsername_UserNotFound() {
        // Arrange
        String email = "notfound@example.com";
        when(memberRepository.findByEmailAndActiveTrue(email)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            authService.loadUserByUsername(email);
        });
        assertEquals("Member with email " + email + " not found", exception.getMessage());
    }

    @Test
    void loadUserByUsername_UserBlocked() {
        // Arrange
        String email = "blocked@example.com";
        Member member = new Member();
        member.setEmail(email);
        member.setPassword("password");
        member.setActive(true);
        member.setBlocked(true);
        member.setRoles(Collections.singletonList("USER"));

        when(memberRepository.findByEmailAndActiveTrue(email)).thenReturn(Optional.of(member));

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            authService.loadUserByUsername(email);
        });
        assertEquals("Member with email " + email + " is blocked", exception.getMessage());
    }
}
