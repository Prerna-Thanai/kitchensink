package com.kitchensink.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.kitchensink.dto.MemberDto;
import com.kitchensink.dto.UpdateMemberRequest;
import com.kitchensink.entity.Member;
import com.kitchensink.enums.ErrorType;
import com.kitchensink.exception.AppAuthenticationException;
import com.kitchensink.repository.MemberRepository;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private MemberServiceImpl memberService;

    private Member mockMember;

    @Value("${phone.validation.key}")
    private String phoneValidationKey = "dummy-key";

    private static final String PHONE_VALIDATION_URL = "https://api.example.com/validate?apikey=";

    @BeforeEach
    void setUp() {
        mockMember = new Member();
        mockMember.setId("123");
        mockMember.setEmail("test@example.com");
        mockMember.setName("Test User");
        mockMember.setPhoneNumber("1234567890");
        mockMember.setActive(true);
        mockMember.setBlocked(false);
        mockMember.setRoles(List.of("ROLE_USER"));
        mockMember.setCreatedAt(LocalDateTime.now());

        ReflectionTestUtils.setField(memberService, "phoneValidationKey", "test-api-key");
        ReflectionTestUtils.setField(memberService, "restTemplate", restTemplate);

    }

    @Test
    void testCurrentUserData_Success() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(new User(mockMember.getEmail(), "password", List.of()));

        when(memberRepository.findByEmail(mockMember.getEmail())).thenReturn(Optional.of(mockMember));

        MemberDto dto = memberService.currentUserData(auth);

        assertThat(dto.getEmail()).isEqualTo(mockMember.getEmail());
        assertThat(dto.getName()).isEqualTo(mockMember.getName());
    }

    @Test
    void testCurrentUserData_Unauthenticated() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);

        assertThatThrownBy(() -> memberService.currentUserData(auth)).isInstanceOf(AppAuthenticationException.class)
            .hasMessageContaining("Member not authenticated");
    }

    @Test
    void testGetAllMembers_ShowInactiveTrue() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Member> page = new PageImpl<>(List.of(mockMember));
        when(memberRepository.findAll(pageable)).thenReturn(page);

        Page<MemberDto> result = memberService.getAllMembers(pageable, true);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo(mockMember.getEmail());
    }

    @Test
    void testGetAllMembers_ShowInactiveFalse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Member> page = new PageImpl<>(List.of(mockMember));
        when(memberRepository.findByActiveTrue(pageable)).thenReturn(page);

        Page<MemberDto> result = memberService.getAllMembers(pageable, false);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEmail()).isEqualTo(mockMember.getEmail());
    }

    @Test
    void testDeleteMemberById_Success() {
        when(memberRepository.findById("123")).thenReturn(Optional.of(mockMember));

        memberService.deleteMemberById("123");

        assertThat(mockMember.isActive()).isFalse();
        verify(memberRepository).save(mockMember);
    }

    @Test
    void testDeleteMemberById_NotFound() {
        when(memberRepository.findById("123")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.deleteMemberById("123")).isInstanceOf(AppAuthenticationException.class)
            .hasMessageContaining("Member with memberId 123 doesn't exist");
    }

    @Test
    void testUpdateMemberDetails_Success() {
        UpdateMemberRequest updateRequest = new UpdateMemberRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setPhoneNumber("1234567890");
        updateRequest.setRoles(List.of("ROLE_ADMIN"));
        updateRequest.setUnBlockMember(true);

        when(memberRepository.findById("123")).thenReturn(Optional.of(mockMember));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MemberDto result = memberService.updateMemberDetails("123", updateRequest);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getPhoneNumber()).isEqualTo("1234567890");
        assertThat(result.getRoles()).contains("ROLE_ADMIN");
        assertThat(result.isBlocked()).isFalse();
    }

    @Test
    void testUpdateMemberDetails_NotFound() {
        UpdateMemberRequest updateRequest = new UpdateMemberRequest();
        when(memberRepository.findById("notfound")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.updateMemberDetails("notfound", updateRequest)).isInstanceOf(
            AppAuthenticationException.class).hasMessageContaining("Member with memberId notfound doesn't exist");
    }

    @Test
    void validatePhoneNumber_ValidPhone_DoesNotThrow() throws Exception {
        // Mock successful validation
        String phoneNumber = "9876543210";
        String json = "{\"valid\":true}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(json, HttpStatus.OK);

        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(responseEntity);

        assertDoesNotThrow(() -> memberService.validatePhoneNumber(phoneNumber));
    }

    @Test
    void validatePhoneNumber_InvalidPhone_ThrowsException() throws Exception {
        // Mock invalid phone number
        String phoneNumber = "1234567890";
        String json = "{\"valid\":false}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(json, HttpStatus.OK);

        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(responseEntity);

        AppAuthenticationException ex = assertThrows(AppAuthenticationException.class, () -> memberService
            .validatePhoneNumber(phoneNumber));

        assertEquals("Invalid phone number: " + phoneNumber, ex.getMessage());
        assertEquals(ErrorType.PHONE_NUMBER_INVALID, ex.getErrorType());
    }

    @Test
    void validatePhoneNumber_ApiThrowsException_ThrowsValidationException() {
        // Simulate API failure
        String phoneNumber = "1234567890";
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenThrow(new RuntimeException("API failure"));

        AppAuthenticationException ex = assertThrows(AppAuthenticationException.class, () -> memberService
            .validatePhoneNumber(phoneNumber));

        assertEquals("Invalid phone number: " + phoneNumber, ex.getMessage());
        assertEquals(ErrorType.PHONE_NUMBER_INVALID, ex.getErrorType());
    }
}
