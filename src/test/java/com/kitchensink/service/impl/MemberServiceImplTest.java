package com.kitchensink.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.client.RestTemplate;

import com.kitchensink.dto.MemberDto;
import com.kitchensink.dto.MemberSearchCriteria;
import com.kitchensink.dto.UpdateMemberRequest;
import com.kitchensink.entity.Member;
import com.kitchensink.enums.ErrorType;
import com.kitchensink.exception.AppAuthenticationException;
import com.kitchensink.exception.BaseApplicationException;
import com.kitchensink.repository.MemberRepository;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private MongoTemplate mongoTemplate;

    private MemberServiceImpl memberService;

    private Member mockMember;
    private Member mockSameMember;
    private Pageable pageable;

    @Value("${phone.validation.key}")
    private String phoneValidationKey = "dummy-key";

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
        mockMember.setUpdatedAt(LocalDateTime.now());

        mockSameMember = new Member();
        mockSameMember.setId("1234");
        mockSameMember.setEmail("test@example.com");
        mockSameMember.setName("Test User");
        mockSameMember.setPhoneNumber("1234567890");
        mockSameMember.setActive(true);
        mockSameMember.setBlocked(false);
        mockSameMember.setRoles(List.of("ROLE_USER"));
        mockSameMember.setCreatedAt(LocalDateTime.now());
        mockSameMember.setUpdatedAt(LocalDateTime.now());

        memberService = new MemberServiceImpl(memberRepository, restTemplate, mongoTemplate, true, phoneValidationKey);
        pageable = PageRequest.of(0, 10, Sort.by("name"));

    }

    @Test
    void testCurrentUserData_Success() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(new User(mockMember.getEmail(), "password", List.of()));

        when(memberRepository.findByEmail(mockMember.getEmail())).thenReturn(Optional.of(mockMember));

        MemberDto memberDto = new MemberDto();
        memberDto.setId(mockMember.getId());
        memberDto.setName(mockMember.getName());
        memberDto.setEmail(mockMember.getEmail());
        memberDto.setPhoneNumber(mockMember.getPhoneNumber());
        memberDto.setRoles(mockMember.getRoles());
        memberDto.setJoiningDate(mockMember.getCreatedAt().toLocalDate());
        memberDto.setActive(mockMember.isActive());
        memberDto.setBlocked(mockMember.isBlocked());
        memberDto.setRoles(new ArrayList<>(mockMember.getRoles()));
        MemberDto dto = memberService.currentUserData(auth);

        assertEquals(memberDto, dto);
        assertEquals(memberDto.hashCode(), dto.hashCode());
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
        when(memberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockSameMember));

        memberService.deleteMemberById("123", getAuthForSuccess());

        assertThat(mockMember.isActive()).isFalse();
        verify(memberRepository).save(mockMember);
    }

    @Test
    void testDeleteMemberById_NotFound() {
        when(memberRepository.findById("123")).thenReturn(Optional.empty());
        when(memberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockSameMember));

        assertThatThrownBy(() -> memberService.deleteMemberById("123", getAuthForSuccess())).isInstanceOf(
            AppAuthenticationException.class).hasMessageContaining("Member with memberId 123 doesn't exist");
    }

    @Test
    void testUpdateMemberDetails_Success() {
        UpdateMemberRequest updateRequest = new UpdateMemberRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setPhoneNumber("1234567890");
        updateRequest.setRoles(List.of("ROLE_ADMIN"));
        updateRequest.setUnBlockMember(true);

        when(memberRepository.findById("123")).thenReturn(Optional.of(mockMember));
        when(memberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockSameMember));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MemberDto result = memberService.updateMemberDetails("123", getAuthForSuccess(), updateRequest);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getPhoneNumber()).isEqualTo("1234567890");
        assertThat(result.getRoles()).contains("ROLE_ADMIN");
        assertThat(result.isBlocked()).isFalse();
    }

    @Test
    void testUpdateMemberDetailsWithNumber_Success() {
        UpdateMemberRequest updateRequest = new UpdateMemberRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setPhoneNumber("1234567899");
        updateRequest.setRoles(List.of("ROLE_ADMIN"));
        updateRequest.setUnBlockMember(true);

        when(memberRepository.findById("123")).thenReturn(Optional.of(mockMember));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(new ResponseEntity<>(
            "{\"valid\":true}", HttpStatus.OK));
        when(memberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockSameMember));

        MemberDto result = memberService.updateMemberDetails("123", getAuthForSuccess(), updateRequest);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getPhoneNumber()).isEqualTo("1234567899");
        assertThat(result.getRoles()).contains("ROLE_ADMIN");
        assertThat(result.isBlocked()).isFalse();
    }

    @Test
    void testUpdateMemberDetails_NotFound() {
        UpdateMemberRequest updateRequest = new UpdateMemberRequest();
        when(memberRepository.findById("notfound")).thenReturn(Optional.empty());
        when(memberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockSameMember));

        assertThatThrownBy(() -> memberService.updateMemberDetails("notfound", getAuthForSuccess(), updateRequest))
            .isInstanceOf(AppAuthenticationException.class).hasMessageContaining(
                "Member with memberId notfound doesn't exist");
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

        BaseApplicationException ex = assertThrows(BaseApplicationException.class, () -> memberService
            .validatePhoneNumber(phoneNumber));

        assertEquals("Invalid phone number: " + phoneNumber, ex.getMessage());
        assertEquals(ErrorType.PHONE_NUMBER_INVALID, ex.getErrorType());
    }

    @Test
    void validatePhoneNumber_ApiThrowsException_ThrowsValidationException() {
        // Simulate API failure
        String phoneNumber = "1234567890";
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenThrow(new RuntimeException("API failure"));

        BaseApplicationException ex = assertThrows(BaseApplicationException.class, () -> memberService
            .validatePhoneNumber(phoneNumber));

        assertEquals("Invalid phone number: " + phoneNumber, ex.getMessage());
        assertEquals(ErrorType.PHONE_NUMBER_INVALID, ex.getErrorType());
    }

    @Test
    void testGetFilteredMembersByCriteria_NameAndEmail() {
        MemberSearchCriteria criteria = new MemberSearchCriteria();
        criteria.setName("John");
        criteria.setEmail("john@example.com");

        List<Member> mockMembers = List.of(createMember("John Doe", "john@example.com", List.of("ADMIN"), true),
            createMember("Johnny", "johnny@example.com", List.of("USER"), true));

        when(mongoTemplate.find(any(Query.class), eq(Member.class))).thenReturn(mockMembers);
        when(mongoTemplate.count(any(Query.class), eq(Member.class))).thenReturn((long) mockMembers.size());

        Page<MemberDto> result = memberService.getFilteredMembersByCriteria(pageable, false, criteria);

        assertEquals(2, result.getTotalElements());
        assertEquals("John Doe", result.getContent().get(0).getName());
        assertEquals("johnny@example.com", result.getContent().get(1).getEmail());
    }

    @Test
    void testGetFilteredMembersByCriteria_RoleOnly() {
        MemberSearchCriteria criteria = new MemberSearchCriteria();
        criteria.setRole("ADMIN");

        List<Member> mockMembers = List.of(createMember("Alice", "alice@admin.com", List.of("ADMIN"), true));

        when(mongoTemplate.find(any(Query.class), eq(Member.class))).thenReturn(mockMembers);
        when(mongoTemplate.count(any(Query.class), eq(Member.class))).thenReturn(1L);

        Page<MemberDto> result = memberService.getFilteredMembersByCriteria(pageable, false, criteria);

        assertEquals(1, result.getTotalElements());
        assertEquals("ADMIN", result.getContent().get(0).getRoles().get(0));
    }

    @Test
    void testGetFilteredMembersByCriteria_NameEmailRole() {
        MemberSearchCriteria criteria = new MemberSearchCriteria();
        criteria.setName("Bob");
        criteria.setEmail("bob@example.com");
        criteria.setRole("USER");

        List<Member> mockMembers = List.of(createMember("Bob", "bob@example.com", List.of("USER"), true));

        when(mongoTemplate.find(any(Query.class), eq(Member.class))).thenReturn(mockMembers);
        when(mongoTemplate.count(any(Query.class), eq(Member.class))).thenReturn(1L);

        Page<MemberDto> result = memberService.getFilteredMembersByCriteria(pageable, false, criteria);

        assertEquals(1, result.getTotalElements());
        assertEquals("Bob", result.getContent().get(0).getName());
        assertTrue(result.getContent().get(0).getRoles().contains("USER"));
    }

    @Test
    void testGetFilteredMembersByCriteria_EmptyCriteria() {
        MemberSearchCriteria criteria = new MemberSearchCriteria();

        List<Member> mockMembers = List.of(createMember("Charlie", "charlie@example.com", List.of("USER"), true),
            createMember("Dave", "dave@example.com", List.of("ADMIN"), false));

        when(mongoTemplate.find(any(Query.class), eq(Member.class))).thenReturn(mockMembers);
        when(mongoTemplate.count(any(Query.class), eq(Member.class))).thenReturn((long) mockMembers.size());

        Page<MemberDto> result = memberService.getFilteredMembersByCriteria(pageable, false, criteria);

        assertEquals(2, result.getTotalElements());
    }

    @Test
    void testGetFilteredMembersByCriteria_NoResults() {
        MemberSearchCriteria criteria = new MemberSearchCriteria();
        criteria.setName("NonExistent");

        when(mongoTemplate.find(any(Query.class), eq(Member.class))).thenReturn(Collections.emptyList());
        when(mongoTemplate.count(any(Query.class), eq(Member.class))).thenReturn(0L);

        Page<MemberDto> result = memberService.getFilteredMembersByCriteria(pageable, false, criteria);

        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void testGetFilteredMembersByCriteria_RoleAndInactive() {
        MemberSearchCriteria criteria = new MemberSearchCriteria();
        criteria.setRole("ADMIN");

        List<Member> mockMembers = List.of(createMember("Eve", "eve@admin.com", List.of("ADMIN"), false));

        when(mongoTemplate.find(any(Query.class), eq(Member.class))).thenReturn(mockMembers);
        when(mongoTemplate.count(any(Query.class), eq(Member.class))).thenReturn(1L);

        Page<MemberDto> result = memberService.getFilteredMembersByCriteria(pageable, true, criteria);

        assertEquals(1, result.getTotalElements());
        assertEquals("Eve", result.getContent().get(0).getName());
    }

    private Member createMember(String name, String email, List<String> roles, boolean active) {
        Member member = new Member();
        member.setId(UUID.randomUUID().toString());
        member.setName(name);
        member.setEmail(email);
        member.setRoles(roles);
        member.setActive(active);
        member.setCreatedAt(LocalDateTime.now());
        return member;
    }

    private Authentication getAuthForSuccess() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(new User(mockMember.getEmail(), "password", List.of()));
        return auth;
    }
}
