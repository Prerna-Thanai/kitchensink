package com.kitchensink.service.impl;

import com.kitchensink.dto.MemberDto;
import com.kitchensink.dto.UpdateMemberRequest;
import com.kitchensink.entity.Member;
import com.kitchensink.exception.AppAuthenticationException;
import com.kitchensink.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberServiceImpl memberService;

    private Member mockMember;

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

        assertThatThrownBy(() -> memberService.currentUserData(auth))
            .isInstanceOf(AppAuthenticationException.class)
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

        assertThatThrownBy(() -> memberService.deleteMemberById("123"))
            .isInstanceOf(AppAuthenticationException.class)
            .hasMessageContaining("Member with memberId 123 doesn't exist");
    }

    @Test
    void testUpdateMemberDetails_Success() {
        UpdateMemberRequest updateRequest = new UpdateMemberRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setPhoneNumber("9876543210");
        updateRequest.setRoles(List.of("ROLE_ADMIN"));
        updateRequest.setUnBlockMember(true);

        when(memberRepository.findById("123")).thenReturn(Optional.of(mockMember));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MemberDto result = memberService.updateMemberDetails("123", updateRequest);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getPhoneNumber()).isEqualTo("9876543210");
        assertThat(result.getRoles()).contains("ROLE_ADMIN");
        assertThat(result.isBlocked()).isFalse();
    }

    @Test
    void testUpdateMemberDetails_NotFound() {
        UpdateMemberRequest updateRequest = new UpdateMemberRequest();
        when(memberRepository.findById("notfound")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.updateMemberDetails("notfound", updateRequest))
            .isInstanceOf(AppAuthenticationException.class)
            .hasMessageContaining("Member with memberId notfound doesn't exist");
    }
}
