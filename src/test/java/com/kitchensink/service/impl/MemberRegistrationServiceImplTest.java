package com.kitchensink.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import com.kitchensink.dto.RegisterMemberDto;
import com.kitchensink.entity.Member;
import com.kitchensink.enums.ErrorType;
import com.kitchensink.exception.ConflictException;
import com.kitchensink.repository.MemberRepository;

public class MemberRegistrationServiceImplTest {

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    MemberRepository memberRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    MemberRegistrationServiceImpl registrationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // @Test
    void testRegister_Success() {
        RegisterMemberDto dto = RegisterMemberDto.builder().email("test@example.com").name("John").password("secret")
            .phoneNumber("1234567890").roles(Arrays.asList("USER")).build();

        Member insertedMember = Member.builder().email(dto.getEmail()).phoneNumber(dto.getPhoneNumber()).name(dto
            .getName()).active(true).password("encoded-password").roles(dto.getRoles()).build();

        when(memberRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(memberRepository.findByPhoneNumber(dto.getPhoneNumber())).thenReturn(null);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encoded-password");
        when(memberRepository.insert(any(Member.class))).thenReturn(insertedMember);

        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuth);

        Authentication result = registrationService.register(dto);
        assertNotNull(result);
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        RegisterMemberDto dto = RegisterMemberDto.builder().email("existing@example.com").build();

        when(memberRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(new Member()));

        ConflictException ex = assertThrows(ConflictException.class, () -> registrationService.register(dto));
        assertEquals(ErrorType.EMAIL_ALREADY_REGISTERED, ex.getErrorType());
    }

    @Test
    void testRegister_PhoneAlreadyExists() {
        RegisterMemberDto dto = RegisterMemberDto.builder().email("new@example.com").phoneNumber("9999999999").build();

        when(memberRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(memberRepository.findByPhoneNumber(dto.getPhoneNumber())).thenReturn(new Member());

        ConflictException ex = assertThrows(ConflictException.class, () -> registrationService.register(dto));
        assertEquals(ErrorType.USER_ALREADY_EXISTS, ex.getErrorType());
    }

    @Test
    void testEncryptPassword() {
        String raw = "rawPass";
        String encoded = "encodedPass";

        when(passwordEncoder.encode(raw)).thenReturn(encoded);

        // Use reflection to invoke private method
        var method = assertDoesNotThrow(() -> MemberRegistrationServiceImpl.class.getDeclaredMethod("encryptPassword",
            String.class));
        method.setAccessible(true);

        String result = assertDoesNotThrow(() -> (String) method.invoke(registrationService, raw));

        assertEquals(encoded, result);
    }

    @Test
    void testAuthenticate() {
        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);

        var method = assertDoesNotThrow(() -> MemberRegistrationServiceImpl.class.getDeclaredMethod("authenticate",
            String.class, String.class));
        method.setAccessible(true);

        Authentication result = assertDoesNotThrow(() -> (Authentication) method.invoke(registrationService, "user",
            "pass"));

        assertNotNull(result);
    }

    // @Test
    void testValidatePhone_ValidResponse() throws Exception {
        String phone = "1234567890";
        String body = "{\"valid\": true}";

        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(ResponseEntity.ok(body));

        var method = MemberRegistrationServiceImpl.class.getDeclaredMethod("validatePhone", String.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(registrationService, phone);
        assertTrue(result);
    }

    @Test
    void testValidatePhone_InvalidResponseOrException() throws Exception {
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenThrow(new RuntimeException("fail"));

        var method = MemberRegistrationServiceImpl.class.getDeclaredMethod("validatePhone", String.class);
        method.setAccessible(true);

        boolean result = (boolean) method.invoke(registrationService, "invalid-phone");
        assertFalse(result);
    }
}
