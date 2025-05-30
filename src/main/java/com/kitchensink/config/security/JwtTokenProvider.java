package com.kitchensink.config.security;

import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.kitchensink.enums.ErrorType;
import com.kitchensink.exception.AuthenticationException;
import com.kitchensink.service.impl.AuthServiceImpl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtTokenProvider {

    public static final String TOKEN_TYPE_CLAIM = "token_type";
    public static final String ACCESS_TOKEN = "access";
    public static final String REFRESH_TOKEN = "refresh";

    private final AuthServiceImpl authService;

    @Getter
    private final Duration jwtAccessExpiration;

    @Getter
    private final Duration jwtRefreshExpiration;

    private final Key key;

    public JwtTokenProvider(AuthServiceImpl authService,
        @Value("${jwt.access.expiration:24h}") Duration jwtAccessExpiration,
        @Value("${jwt.refresh.expiration:7d}") Duration jwtRefreshExpiration,
        @Value("${jwt.secret:app$3cr37$##}") String jwtSecret) {
        this.authService = authService;
        this.jwtAccessExpiration = jwtAccessExpiration;
        this.jwtRefreshExpiration = jwtRefreshExpiration;
        key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generate token.
     *
     * @param authentication
     *            the authentication
     * @return the string
     */
    public String generateAccessToken(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalArgumentException("Authentication is null");
        }
        if (authentication.getPrincipal()instanceof UserDetails userDetails) {
            List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtAccessExpiration.toMillis());

            return Jwts.builder().setSubject(userDetails.getUsername()).setIssuedAt(now).setExpiration(expiryDate)
                .claim("roles", roles).claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN).signWith(key, SignatureAlgorithm.HS256)
                .compact();
        }
        throw new IllegalArgumentException("Authentication principal is not an instance of UserDetails");
    }

    public String generateRefreshToken(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Authentication is null or not authenticated");
        }
        if (authentication.getPrincipal()instanceof UserDetails userDetails) {
            List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtAccessExpiration.toMillis());

            return Jwts.builder().setSubject(userDetails.getUsername()).setIssuedAt(now).setExpiration(expiryDate)
                .claim("roles", roles).claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN).signWith(key, SignatureAlgorithm.HS256)
                .compact();
        }
        throw new IllegalArgumentException("Authentication principal is not an instance of UserDetails");
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    private String getUsernameFromClaims(Claims tokenClaims) {
        return tokenClaims.getSubject();
    }

    private boolean isTokenExpired(Claims tokenClaims) {
        Date expiration = tokenClaims.getExpiration();
        return expiration != null && expiration.after(new Date());
    }

    public void validateAccessToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new AuthenticationException("Token is missing", ErrorType.TOKEN_NOT_FOUND);
        }
        validateToken(null, token, ACCESS_TOKEN);
    }

    public void validateRefreshToken(Authentication authentication, String token) {
        // if (authentication == null || !authentication.isAuthenticated()) {
        // throw new AuthenticationException("Member not authenticated", ErrorType.MEMBER_NOT_AUTHENTICATED);
        // }
        validateToken(authentication, token, REFRESH_TOKEN);
    }

    private void validateToken(Authentication authentication, String token, String tokenType) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            String tokenTypeInClaim = claims.get(TOKEN_TYPE_CLAIM, String.class);
            if (!tokenType.equals(tokenTypeInClaim)) {
                throw new AuthenticationException("Refresh Token not found", ErrorType.TOKEN_INVALID);
            }
            if (!isTokenExpired(claims)) {
                throw new AuthenticationException("Token is expired", ErrorType.TOKEN_EXPIRED);
            }
            if (authentication != null && !getUsernameFromClaims(claims).equals(authentication.getName())) {
                throw new AuthenticationException("Invalid user", ErrorType.TOKEN_INVALID);
            }

            if (authentication != null) {
                authService.loadUserByUsername(authentication.getName());
            }
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT Refresh token: {}", token, e);
            throw new AuthenticationException("Invalid Token", ErrorType.TOKEN_INVALID);
        }
    }
}
