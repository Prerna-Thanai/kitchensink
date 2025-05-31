package com.kitchensink.api;

import com.kitchensink.dto.MemberDto;
import com.kitchensink.dto.UpdateMemberRequest;
import com.kitchensink.service.MemberService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @Test
    @WithMockUser
    void testCurrentUserData() throws Exception {
        MemberDto dto = new MemberDto();
        dto.setId("123");
        Mockito.when(memberService.currentUserData(any())).thenReturn(dto);

        mockMvc.perform(get("/api/members/current"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("123"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllMembers() throws Exception {
        MemberDto member = new MemberDto();
        member.setId("1");
        PageImpl<MemberDto> page = new PageImpl<>(List.of(member));
        Mockito.when(memberService.getAllMembers(any(), eq(false))).thenReturn(page);

        mockMvc.perform(get("/api/members/?showInactiveMembers=false"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value("1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUserById() throws Exception {
        mockMvc.perform(delete("/api/members/123"))
            .andExpect(status().isOk());
        Mockito.verify(memberService).deleteMemberById("123");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateUserById() throws Exception {
        MemberDto updatedDto = new MemberDto();
        updatedDto.setId("123");
        Mockito.when(memberService.updateMemberDetails(eq("123"), any(UpdateMemberRequest.class)))
               .thenReturn(updatedDto);

        mockMvc.perform(put("/api/members/123")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"newname\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("123"));
    }
}
