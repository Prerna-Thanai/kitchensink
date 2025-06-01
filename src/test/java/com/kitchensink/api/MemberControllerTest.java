package com.kitchensink.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kitchensink.config.security.JwtTokenProvider;
import com.kitchensink.dto.MemberDto;
import com.kitchensink.dto.MemberSearchCriteria;
import com.kitchensink.service.MemberService;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtTokenProvider tokenProvider;

    private MemberDto member1;
    private MemberDto member2;

    @BeforeEach
    void setUp() {
        member1 = new MemberDto();
        member1.setId("1");
        member1.setName("Alice");
        member1.setEmail("alice@example.com");
        member1.setRoles(List.of("ADMIN"));

        member2 = new MemberDto();
        member2.setId("2");
        member2.setName("Bob");
        member2.setEmail("bob@example.com");
        member2.setRoles(List.of("USER"));
    }

    @Test
    @WithMockUser
    void testCurrentUserData() throws Exception {
        MemberDto dto = new MemberDto();
        dto.setId("123");
        Mockito.when(memberService.currentUserData(any())).thenReturn(dto);

        mockMvc.perform(get("/api/members/current")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(
            "123"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getFilteredMembersByCriteria_returnsPagedResult() throws Exception {
        MemberSearchCriteria criteria = new MemberSearchCriteria();
        criteria.setName("Alice");

        Page<MemberDto> page = new PageImpl<>(List.of(member1), PageRequest.of(0, 10), 1);

        when(memberService.getFilteredMembersByCriteria(any(Pageable.class), anyBoolean(), any(
            MemberSearchCriteria.class))).thenReturn(page);

        mockMvc.perform(post("/api/members/search?page=0&size=10&showInactiveMembers=false").contentType(
            MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(criteria))).andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("Alice")).andExpect(jsonPath("$.content[0].roles[0]").value(
                "ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getFilteredMembersByCriteria_withRoleFilter() throws Exception {
        MemberSearchCriteria criteria = new MemberSearchCriteria();
        criteria.setRole("USER");

        Page<MemberDto> page = new PageImpl<>(List.of(member2), PageRequest.of(0, 10), 1);

        when(memberService.getFilteredMembersByCriteria(any(Pageable.class), anyBoolean(), any(
            MemberSearchCriteria.class))).thenReturn(page);

        mockMvc.perform(post("/api/members/search?page=0&size=10&showInactiveMembers=false").contentType(
            MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(criteria))).andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].roles[0]").value("USER"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getFilteredMembersByCriteria_withNoResults() throws Exception {
        MemberSearchCriteria criteria = new MemberSearchCriteria();
        criteria.setName("Nonexistent");

        Page<MemberDto> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        when(memberService.getFilteredMembersByCriteria(any(Pageable.class), anyBoolean(), any(
            MemberSearchCriteria.class))).thenReturn(page);

        mockMvc.perform(post("/api/members/search?page=0&size=10&showInactiveMembers=false").contentType(
            MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(criteria))).andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getFilteredMembersByCriteria_withInactiveMembers() throws Exception {
        MemberSearchCriteria criteria = new MemberSearchCriteria();

        Page<MemberDto> page = new PageImpl<>(List.of(member1, member2), PageRequest.of(0, 10), 2);

        when(memberService.getFilteredMembersByCriteria(any(Pageable.class), eq(true), any(MemberSearchCriteria.class)))
            .thenReturn(page);

        mockMvc.perform(post("/api/members/search?page=0&size=10&showInactiveMembers=true").contentType(
            MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(criteria))).andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void getFilteredMembersByCriteria_unauthorized() throws Exception {
        MemberSearchCriteria criteria = new MemberSearchCriteria();

        mockMvc.perform(post("/api/members/search?page=0&size=10&showInactiveMembers=false").contentType(
            MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(criteria))).andExpect(status()
                .isForbidden());
    }
}
