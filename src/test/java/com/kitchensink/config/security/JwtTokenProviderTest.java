package com.kitchensink.config.security;

import com.kitchensink.enums.ErrorType;
import com.kitchensink.exception.AppAuthenticationException;
import com.kitchensink.service.impl.AuthServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @Mock
    private AuthServiceImpl mockAuthService;

    private JwtTokenProvider jwtTokenProvider;

    private final String jwtSecret = "testBasicSecretKeyForJwtTokenProvider"; // Must be long enough
    private final Duration jwtAccessExpiration = Duration.ofMinutes(30);
    private final Duration jwtRefreshExpiration = Duration.ofHours(8);
    private Key signingKey;


    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                mockAuthService,
                jwtAccessExpiration,
                jwtRefreshExpiration,
                jwtSecret
        );
        signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // Helper to create Authentication
    private Authentication createMockAuthentication(String username, List<String> roles, boolean isAuthenticated) {
        List<SimpleGrantedAuthority> authorities = roles.stream().map(SimpleGrantedAuthority::new).toList();
        UserDetails userDetails = new User(username, "password", authorities);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword());
        ReflectionTestUtils.setField(authentication, "authenticated", isAuthenticated);
        return authentication;
    }

    // Helper to generate a token for testing validation
    private String generateTestToken(String subject, Duration expiration, String tokenType, Key keyToUse) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration.toMillis());
        return Jwts.builder()
                   .setSubject(subject)
                   .setIssuedAt(now)
                   .setExpiration(expiryDate)
                   .claim(JwtTokenProvider.TOKEN_TYPE_CLAIM, tokenType)
                   .claim("roles", Collections.singletonList("ROLE_USER")) // Add roles for completeness
                   .signWith(keyToUse, SignatureAlgorithm.HS256)
                   .compact();
    }

    @Test
    void generateAccessToken_success() {
        Authentication authentication = createMockAuthentication("user1", Collections.singletonList("ROLE_USER"), true);
        String token = jwtTokenProvider.generateAccessToken(authentication);
        assertNotNull(token);
        // Optionally verify claims if needed, but keeping it basic
        Claims claims = Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();
        assertEquals("user1", claims.getSubject());
        assertEquals(JwtTokenProvider.ACCESS_TOKEN, claims.get(JwtTokenProvider.TOKEN_TYPE_CLAIM));
    }

    @Test
    void generateAccessToken_nullAuthentication_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> jwtTokenProvider.generateAccessToken(null));
    }

    @Test
    void generateRefreshToken_success() {
        Authentication authentication = createMockAuthentication("user1", Collections.singletonList("ROLE_USER"), true);
        String token = jwtTokenProvider.generateRefreshToken(authentication);
        assertNotNull(token);
        Claims claims = Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();
        assertEquals("user1", claims.getSubject());
        assertEquals(JwtTokenProvider.REFRESH_TOKEN, claims.get(JwtTokenProvider.TOKEN_TYPE_CLAIM));
    }

    @Test
    void generateRefreshToken_nullAuthentication_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> jwtTokenProvider.generateRefreshToken(null));
    }

    @Test
    void generateRefreshToken_notAuthenticated_throwsException() {
        Authentication authentication = createMockAuthentication("user1", Collections.emptyList(), false);
        assertThrows(IllegalArgumentException.class, () -> jwtTokenProvider.generateRefreshToken(authentication));
    }

    @Test
    void getUsernameFromToken_success() {
        String expectedUsername = "userFromToken";
        String token = generateTestToken(expectedUsername, jwtAccessExpiration, JwtTokenProvider.ACCESS_TOKEN, signingKey);
        String actualUsername = jwtTokenProvider.getUsernameFromToken(token);
        assertEquals(expectedUsername, actualUsername);
    }

    @Test
    void validateAccessToken_success() {
        String token = generateTestToken("validUser", jwtAccessExpiration, JwtTokenProvider.ACCESS_TOKEN, signingKey);
        jwtTokenProvider.validateAccessToken(token); // No exception
    }

    @Test
    void validateAccessToken_missingToken_throwsException() {
        AppAuthenticationException ex = assertThrows(AppAuthenticationException.class, () -> jwtTokenProvider.validateAccessToken(null));
        assertEquals(ErrorType.TOKEN_NOT_FOUND, ex.getErrorType());
    }

    @Test
    void validateAccessToken_expired_throwsException() {
        String token = generateTestToken("expiredUser", Duration.ofMillis(-1000), JwtTokenProvider.ACCESS_TOKEN, signingKey);
        AppAuthenticationException ex = assertThrows(AppAuthenticationException.class, () -> jwtTokenProvider.validateAccessToken(token));
        assertEquals(ErrorType.TOKEN_EXPIRED, ex.getErrorType());
    }

    @Test
    void validateAccessToken_wrongType_throwsException() {
        String token = generateTestToken("userWithWrongType", jwtAccessExpiration, JwtTokenProvider.REFRESH_TOKEN, signingKey);
        // This specific message is because "Refresh Token not found" is thrown if type doesn't match,
        // but then validateAccessToken wraps it if the expected type is ACCESS_TOKEN.
        // The generic catch block in validateToken leads to "Invalid Token"
        AppAuthenticationException ex = assertThrows(AppAuthenticationException.class, () -> jwtTokenProvider.validateAccessToken(token));
        assertEquals(ErrorType.TOKEN_INVALID, ex.getErrorType());
        assertEquals("access token not found", ex.getMessage());
    }


    @Test
    void validateRefreshToken_success() {
        Authentication authentication = createMockAuthentication("validUser", Collections.emptyList(), true);
        when(mockAuthService.loadUserByUsername("validUser")).thenReturn(mock(UserDetails.class));
        String token = generateTestToken("validUser", jwtRefreshExpiration, JwtTokenProvider.REFRESH_TOKEN, signingKey);

        jwtTokenProvider.validateRefreshToken(authentication, token); // No exception
        verify(mockAuthService).loadUserByUsername("validUser");
    }

    @Test
    void validateRefreshToken_expired_throwsException() {
        Authentication authentication = createMockAuthentication("userWithExpiredRefresh", Collections.emptyList(), true);
        String token = generateTestToken("userWithExpiredRefresh", Duration.ofMillis(-1000), JwtTokenProvider.REFRESH_TOKEN, signingKey);

        AppAuthenticationException ex = assertThrows(AppAuthenticationException.class, () -> jwtTokenProvider.validateRefreshToken(authentication, token));
        assertEquals(ErrorType.TOKEN_EXPIRED, ex.getErrorType());
        verify(mockAuthService, never()).loadUserByUsername(anyString());
    }

    @Test
    void validateRefreshToken_usernameMismatch_throwsException() {
        Authentication authentication = createMockAuthentication("actualUser", Collections.emptyList(), true);
        String token = generateTestToken("tokenUser", jwtRefreshExpiration, JwtTokenProvider.REFRESH_TOKEN, signingKey);

        AppAuthenticationException ex = assertThrows(AppAuthenticationException.class, () -> jwtTokenProvider.validateRefreshToken(authentication, token));
        assertEquals(ErrorType.TOKEN_INVALID, ex.getErrorType());
        assertEquals("Invalid user", ex.getMessage());
        verify(mockAuthService, never()).loadUserByUsername(anyString());
    }

    @Test
    void validateRefreshToken_authServiceThrowsException_throwsException() {
        String username = "serviceDownUser";
        Authentication authentication = createMockAuthentication(username, Collections.emptyList(), true);
        when(mockAuthService.loadUserByUsername(username)).thenThrow(new UsernameNotFoundException("Service unavailable"));
        String token = generateTestToken(username, jwtRefreshExpiration, JwtTokenProvider.REFRESH_TOKEN, signingKey);

        assertThrows(UsernameNotFoundException.class, () -> jwtTokenProvider.validateRefreshToken(authentication, token));
        verify(mockAuthService).loadUserByUsername(username);
    }

    @Test
    void validateRefreshToken_wrongType_throwsException() {
        Authentication authentication = createMockAuthentication("userWithWrongType", Collections.emptyList(), true);
        String token = generateTestToken("userWithWrongType", jwtRefreshExpiration, JwtTokenProvider.ACCESS_TOKEN, signingKey);
        AppAuthenticationException ex = assertThrows(AppAuthenticationException.class, () -> jwtTokenProvider.validateRefreshToken(authentication, token));
        assertEquals(ErrorType.TOKEN_INVALID, ex.getErrorType());
        assertEquals("refresh token not found", ex.getMessage()); // Specific message for token type mismatch
        verify(mockAuthService, never()).loadUserByUsername(anyString());
    }
}