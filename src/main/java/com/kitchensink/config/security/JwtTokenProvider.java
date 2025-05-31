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
import com.kitchensink.exception.AppAuthenticationException;
import com.kitchensink.service.impl.AuthServiceImpl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class JwtTokenProvider.
 *
 * @author prerna
 */
@Component
@Slf4j
public class JwtTokenProvider {

    /** The Constant TOKEN_TYPE_CLAIM */
    public static final String TOKEN_TYPE_CLAIM = "token_type";

    /** The Constant ACCESS_TOKEN */
    public static final String ACCESS_TOKEN = "access";

    /** The Constant REFRESH_TOKEN */
    public static final String REFRESH_TOKEN = "refresh";

    /** The auth service */
    private final AuthServiceImpl authService;

    /** The jwt access expiration */
    @Getter
    private final Duration jwtAccessExpiration;

    /** The refresh expiration */
    @Getter
    private final Duration jwtRefreshExpiration;

    /** The key */
    private final Key key;

    /**
     * JwtTokenProvider constructor
     *
     * @param authService
     *            the auth service
     * @param jwtAccessExpiration
     *            the jwt access expiration
     * @param jwtRefreshExpiration
     *            the jwt refresj expiration time
     * @param jwtSecret
     *            the jwtsecret
     */
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

    /**
     * Generate refresh token
     *
     * @param authentication
     *            the authentication
     * @return token
     */
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

    /**
     * Get username from token
     *
     * @param token
     *            the token
     * @return username
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * Get username from claims
     *
     * @param tokenClaims
     *            the token claims
     * @return username
     */
    private String getUsernameFromClaims(Claims tokenClaims) {
        return tokenClaims.getSubject();
    }

    /**
     * Is token expired
     *
     * @param tokenClaims
     *            the token claims
     * @return boolean
     */
    private boolean isTokenExpired(Claims tokenClaims) {
        Date expiration = tokenClaims.getExpiration();
        return expiration != null && expiration.after(new Date());
    }

    /**
     * Validate access token
     *
     * @param token
     *            the token
     */
    public void validateAccessToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new AppAuthenticationException("Token is missing", ErrorType.TOKEN_NOT_FOUND);
        }
        validateToken(null, token, ACCESS_TOKEN);
    }

    /**
     * @param refresh
     *            token
     * @param token
     *            the token
     */
    public void validateRefreshToken(Authentication authentication, String token) {
        validateToken(authentication, token, REFRESH_TOKEN);
    }

    /**
     * Validate token
     *
     * @param authentication
     *            the authentication
     * @param token
     *            the token
     * @param tokenType
     *            the token type
     */
    private void validateToken(Authentication authentication, String token, String tokenType) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            String tokenTypeInClaim = claims.get(TOKEN_TYPE_CLAIM, String.class);
            if (!tokenType.equals(tokenTypeInClaim)) {
                throw new AppAuthenticationException(tokenType + " token not found", ErrorType.TOKEN_INVALID);
            }
            if (!isTokenExpired(claims)) {
                throw new AppAuthenticationException("Token is expired", ErrorType.TOKEN_EXPIRED);
            }
            if (authentication != null && !getUsernameFromClaims(claims).equals(authentication.getName())) {
                throw new AppAuthenticationException("Invalid user", ErrorType.TOKEN_INVALID);
            }

            if (authentication != null) {
                authService.loadUserByUsername(authentication.getName());
            }
        } catch (ExpiredJwtException exception) {
            throw new AppAuthenticationException("Token is expired", ErrorType.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT Refresh token: {}", token, e);
            throw new AppAuthenticationException("Invalid Token", ErrorType.TOKEN_INVALID);
        }
    }
}
