package com.kitchensink.service.impl;

import com.kitchensink.dto.RegisterMemberDto;
import com.kitchensink.entity.Member;
import com.kitchensink.exception.AppAuthenticationException;
import com.kitchensink.exception.ConflictException;
import com.kitchensink.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class MemberRegistrationServiceImplTest{

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private MemberRegistrationServiceImpl registrationService;

    private RegisterMemberDto newMember;

    @BeforeEach
    void setUp(){
        newMember = new RegisterMemberDto();
        newMember.setEmail("test@example.com");
        newMember.setName("Test User");
        newMember.setPassword("password123");
        newMember.setPhoneNumber("1234567890");
        newMember.setRoles(List.of("ROLE_USER"));

        ReflectionTestUtils.setField(registrationService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(registrationService, "phoneValidationKey", "test-api-key");
    }

    @Test
    void testRegister_Success() throws Exception{
        when(memberRepository.findByEmail(newMember.getEmail())).thenReturn(Optional.empty());
        when(memberRepository.findByPhoneNumber(newMember.getPhoneNumber())).thenReturn(null);
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{\"valid\":true}"));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        Authentication result = registrationService.register(newMember);

        assertThat(result).isNotNull();
        verify(memberRepository).insert(any(Member.class));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testRegister_EmailAlreadyExists(){
        when(memberRepository.findByEmail(newMember.getEmail())).thenReturn(Optional.of(new Member()));

        assertThatThrownBy(() -> registrationService.register(newMember))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already registered: test@example.com");
    }

    @Test
    void testRegister_PhoneAlreadyExists(){
        when(memberRepository.findByEmail(newMember.getEmail())).thenReturn(Optional.empty());
        when(memberRepository.findByPhoneNumber(newMember.getPhoneNumber())).thenReturn(new Member());

        assertThatThrownBy(() -> registrationService.register(newMember))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Phone number already registered: 1234567890");
    }

    @Test
    void testRegister_InvalidPhone(){
        when(memberRepository.findByEmail(newMember.getEmail())).thenReturn(Optional.empty());
        when(memberRepository.findByPhoneNumber(newMember.getPhoneNumber())).thenReturn(null);
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{\"valid\":false}"));

        assertThatThrownBy(() -> registrationService.register(newMember))
                .isInstanceOf(AppAuthenticationException.class)
                .hasMessageContaining("Invalid phone number: 1234567890");
    }

    @Test
    void testValidatePhone_ValidationApiFails_ReturnsFalse(){
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("API failure"));

        boolean result = registrationService.validatePhone("1234567890");

        assertThat(result).isFalse();
    }
}
