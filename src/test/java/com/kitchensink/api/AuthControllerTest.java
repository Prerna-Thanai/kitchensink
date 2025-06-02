package com.kitchensink.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kitchensink.config.security.JwtTokenProvider;
import com.kitchensink.dto.LoginRequestDto;
import com.kitchensink.entity.Member;
import com.kitchensink.repository.MemberRepository;
import com.kitchensink.service.LoginService;
import com.kitchensink.service.MemberService;
import jakarta.servlet.http.Cookie;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoginService loginService;

    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private JwtTokenProvider tokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private final String accessToken = "access-token";
    private final String refreshToken = "refresh-token";

    private final Authentication dummyAuth = new UsernamePasswordAuthenticationToken("user", "password");

    @Test
    void testLogin() throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setEmail("user@example.com");
        loginRequest.setPassword("password");

        ArgumentCaptor<LoginRequestDto> loginRequestCaptor = ArgumentCaptor.forClass(LoginRequestDto.class);
        when(loginService.login(loginRequestCaptor.capture())).thenReturn(dummyAuth);
        when(tokenProvider.generateAccessToken(any(Authentication.class))).thenReturn(accessToken);
        when(tokenProvider.generateRefreshToken(any(Authentication.class))).thenReturn(refreshToken);
        when(tokenProvider.getJwtAccessExpiration()).thenReturn(Duration.ofMinutes(15));
        when(tokenProvider.getJwtRefreshExpiration()).thenReturn(Duration.ofDays(7));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(
            objectMapper.writeValueAsString(loginRequest))).andExpect(status().isOk()).andExpect(jsonPath("$.message")
                .value("Logged-in successful")).andExpect(jsonPath("$.accessTokenExpiry").exists()).andExpect(jsonPath(
                    "$.refreshTokenExpiry").exists());

        assertEquals(loginRequestCaptor.getValue(), loginRequest);
        assertEquals(loginRequestCaptor.getValue().hashCode(), loginRequest.hashCode());

    }

    /**
     * Test for in valid endpoint with only refresh token and no access token.
     */
    @Test
    void testNoAccessOnlyRefreshToken() throws Exception {
        doNothing().when(tokenProvider).validateRefreshToken(any(), any());
        when(tokenProvider.generateAccessToken(any())).thenReturn(accessToken);
        when(tokenProvider.generateRefreshToken(any())).thenReturn(refreshToken);
        when(tokenProvider.getUsernameFromToken(any())).thenReturn("test@email.com");
        when(tokenProvider.getJwtAccessExpiration()).thenReturn(Duration.ofMinutes(15));
        when(tokenProvider.getJwtRefreshExpiration()).thenReturn(Duration.ofDays(7));
        when(memberRepository.findByEmailAndActiveTrue(any())).thenReturn(Optional.of(createMember("test@email.com","1324")));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/na-route")
                                              .cookie(new Cookie("refresh_token", refreshToken))
                                              .principal(dummyAuth))
               .andExpect(status().isNotFound())
               .andExpect(header().string(HttpHeaders.SET_COOKIE,
                       anyOf(containsString("access_token="))
               ));
    }

    /**
     * Test for 404 endpoint with no access/refresh token
     */
    @Test
    void testRefreshToken_missingToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/na-route"))
               .andExpect(status().isForbidden());
    }

    /**
     * Test for logout endpoint with valid refresh token.
     */
    @Test
    void testLogout() throws Exception {
        doNothing().when(tokenProvider).validateRefreshToken(any(), any());
        when(tokenProvider.generateAccessToken(any())).thenReturn(accessToken);
        when(tokenProvider.generateRefreshToken(any())).thenReturn(refreshToken);
        when(tokenProvider.getUsernameFromToken(any())).thenReturn("test@email.com");
        when(tokenProvider.getJwtAccessExpiration()).thenReturn(Duration.ofMinutes(15));
        when(tokenProvider.getJwtRefreshExpiration()).thenReturn(Duration.ofDays(7));
        when(memberRepository.findByEmailAndActiveTrue(any())).thenReturn(Optional.of(createMember("test@email.com","1324")));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/logout")
               .cookie(new Cookie("refresh_token", refreshToken)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }

    /**
     * Test for check endpoint with valid refresh token.
     */
    @Test
    void testCheck_authenticated() throws Exception {
        doNothing().when(tokenProvider).validateRefreshToken(any(), any());
        when(tokenProvider.generateAccessToken(any())).thenReturn(accessToken);
        when(tokenProvider.generateRefreshToken(any())).thenReturn(refreshToken);
        when(tokenProvider.getUsernameFromToken(any())).thenReturn("test@email.com");
        when(tokenProvider.getJwtAccessExpiration()).thenReturn(Duration.ofMinutes(15));
        when(tokenProvider.getJwtRefreshExpiration()).thenReturn(Duration.ofDays(7));
        when(memberRepository.findByEmailAndActiveTrue(any())).thenReturn(Optional.of(createMember("test@email.com","1324")));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/auth/check")
                                              .cookie(new Cookie("refresh_token", refreshToken)))
               .andExpect(status().isNotFound());
    }

    /**
     * Test for check endpoint without authentication.
     */
    @Test
    void testCheck_unauthenticated() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/auth/check"))
               .andExpect(status().isForbidden());
    }

    private Member createMember(String email, String password) {
        Member member = new Member();
        member.setId("test-id");
        member.setEmail(email);
        member.setPassword(password);
        member.setActive(true);
        member.setBlocked(false);
        member.setFailedLoginAttempts(0);
        member.setRoles(Collections.emptyList());
        return member;
    }
}
