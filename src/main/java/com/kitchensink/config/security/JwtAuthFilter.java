package com.kitchensink.config.security;

import static com.kitchensink.config.security.SecurityConfig.PUBLIC_URLS;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.kitchensink.enums.ErrorType;
import com.kitchensink.exception.AuthenticationException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws IOException {
        try {
            String path = request.getRequestURI();
            boolean shouldSkip = Arrays.stream(PUBLIC_URLS).anyMatch(path::startsWith);
            if (shouldSkip) {
                filterChain.doFilter(request, response);
                return;
            }
            String token = getTokenFromRequest(request);
            if (token == null || token.isEmpty()) {
                String refreshToken = getRefreshTokenFromRequest(request);
                if (refreshToken == null || refreshToken.isEmpty()) {
                    throw new AuthenticationException("Token is missing", ErrorType.TOKEN_NOT_FOUND);
                }
                jwtTokenProvider.validateRefreshToken(null, refreshToken);
                String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
                String updatedAccessToken = jwtTokenProvider.generateAccessToken(authentication);
                String updatedRefreshToken = jwtTokenProvider.generateRefreshToken(authentication);
                setTokenCookie(response, updatedAccessToken, updatedRefreshToken);
                token = updatedAccessToken;
            }
            jwtTokenProvider.validateAccessToken(token);
            String username = jwtTokenProvider.getUsernameFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails,
                null, userDetails.getAuthorities());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (AuthenticationException e) {
            logger.error("Authentication error:", e);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("Authentication error:", e);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("""
                {"message" : "An error occurred while processing the request."}
                """);
        }
    }

    private String getRefreshTokenFromRequest(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;

    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }

        }
        return null;
    }

    private void setTokenCookie(HttpServletResponse response, String access_token, String refresh_token) {
        Cookie accessTokenCookie = new Cookie("access_token", access_token);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true); // Use only with HTTPS
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge((int) jwtTokenProvider.getJwtAccessExpiration().getSeconds()); // Set max age to
                                                                                                   // match expiry
        response.addCookie(accessTokenCookie);

        Cookie refreshTokenCookie = new Cookie("refresh_token", access_token);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true); // Use only with HTTPS
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) jwtTokenProvider.getJwtRefreshExpiration().getSeconds()); // Set max age to
                                                                                                     // match expiry
        response.addCookie(refreshTokenCookie);
    }

}
