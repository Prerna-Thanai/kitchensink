package com.kitchensink.config.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kitchensink.enums.ErrorType;
import com.kitchensink.exception.AppAuthenticationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Class JwtAuthFilter.
 *
 * @author prerna
 */
@Component
@Slf4j
public class JwtAuthFilter extends GenericFilterBean {

    /** The jwt token provider */
    private final JwtTokenProvider jwtTokenProvider;

    /** The user details service */
    private final UserDetailsService userDetailsService;

    /** The object mapper */
    private final ObjectMapper objectMapper;

    /**
     * JWTAuthFilter constructor
     *
     * @param jwtTokenProvider
     *            the jwt token provider
     * @param userDetailsService
     *            the user details service
     * @param objectMapper
     *            the object mapper
     */
    public JwtAuthFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService,
        ObjectMapper objectMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.objectMapper = objectMapper;
    }

    /**
     * Do Filter request
     *
     * @param servletRequest
     *            the servletRequest
     * @param servletResponse
     *            the servletResponse
     * @param filterChain
     *            the filterChain
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        try {
            String token = fetchAccessTokenFromRequestOrRefreshToken(request, response);
            if (token != null) {
                jwtTokenProvider.validateAccessToken(token);
                String username = jwtTokenProvider.getUsernameFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
            resetAuthenticationAfterRequest();
        } catch (AuthenticationException | AppAuthenticationException e) {
            logger.error("Authentication error:", e);
            response.addCookie(getCookie("access_token", "", 0)); // Clear access token cookie
            response.addCookie(getCookie("refresh_token", "", 0)); // Clear refresh token cookie
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(getErrorBody(e.getMessage(), HttpStatus.UNAUTHORIZED));
        } catch (Exception e) {
            logger.error("Authentication error:", e);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(getErrorBody("An error occurred while processing the request",
                HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * Reset Securitycontext
     */
    private void resetAuthenticationAfterRequest() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    /**
     * Fetch Access token
     *
     * @param request
     *            the request
     * @param response
     *            the response
     * @return access token
     */
    private String fetchAccessTokenFromRequestOrRefreshToken(HttpServletRequest request, HttpServletResponse response) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        String accessToken = null;
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    accessToken = cookie.getValue();
                } else if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }

        }
        if (accessToken == null && refreshToken != null) {
            try {
                jwtTokenProvider.validateRefreshToken(null, refreshToken);
                String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
                accessToken = jwtTokenProvider.generateAccessToken(authentication);
                // add cookie back to response
                response.addCookie(getCookie("access_token", accessToken, jwtTokenProvider.getJwtAccessExpiration().getSeconds()));
            } catch (AuthenticationException e) {
                log.error("Invalid refresh token: {}", refreshToken, e);
                return null;
            }
        }
        return accessToken;
    }

    /**
     * Return Error
     *
     * @param message
     *            the message
     * @param status
     *            the status
     * @return error response
     * @throws JsonProcessingException
     */
    private String getErrorBody(String message, HttpStatus status) throws JsonProcessingException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("message", message);
        body.put("errorType", status == HttpStatus.UNAUTHORIZED ? ErrorType.MEMBER_NOT_AUTHENTICATED
            : ErrorType.UNKNOWN);
        body.put("status", status.value());
        return objectMapper.writeValueAsString(body);
    }

    private Cookie getCookie(String name, String value, long maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) maxAge);
        return cookie;
    }

}
