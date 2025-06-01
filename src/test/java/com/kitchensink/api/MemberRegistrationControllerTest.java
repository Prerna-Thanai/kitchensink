package com.kitchensink.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kitchensink.dto.RegisterMemberDto;
import com.kitchensink.service.MemberRegistrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
class MemberRegistrationControllerTest{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberRegistrationService memberRegistrationService;

    @Test
    void testRegisterMember() throws Exception{


        RegisterMemberDto dto = new RegisterMemberDto();
        dto.setEmail("test@email.com");
        dto.setPassword("Password@123");
        dto.setName("Test User");
        dto.setPhoneNumber("8929032991");
        dto.setRoles(List.of("USER"));

        UserDetails userDetails = User.withUsername(dto.getEmail()).password(dto.getPassword())
                                .authorities(dto.getRoles().stream().map(
                                        SimpleGrantedAuthority::new).toList()).disabled(false).build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, dto.getPassword());
        ReflectionTestUtils.setField(authentication, "authenticated", true);
        ArgumentCaptor<RegisterMemberDto> dtoCaptor = ArgumentCaptor.forClass(RegisterMemberDto.class);
        when(memberRegistrationService.register(dtoCaptor.capture()))
                .thenReturn(authentication);

        String requestBody = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post("/api/auth/register")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(requestBody))
               .andExpect(status().isOk())
               .andExpect(header().string(HttpHeaders.SET_COOKIE,
                       anyOf(containsString("access_token="))
               ))
               .andExpect(jsonPath("$.message").value("Registration successful"))
               .andExpect(jsonPath("$.accessTokenExpiry").isNumber())
               .andExpect(jsonPath("$.refreshTokenExpiry").isNumber());

        assertEquals(dtoCaptor.getValue(), dto, "Captured DTO should match the original DTO");
        assertEquals(dtoCaptor.getValue().hashCode(), dto.hashCode());
    }
}