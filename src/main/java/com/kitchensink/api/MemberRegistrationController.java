package com.kitchensink.api;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kitchensink.config.security.JwtTokenProvider;
import com.kitchensink.dto.RegisterMemberDto;
import com.kitchensink.service.MemberRegistrationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

/**
 * The Class MemberRegistrationController.
 *
 * @author prerna
 */
@RestController
@RequestMapping(value = "/api/auth")
public class MemberRegistrationController {

    /** The member registration service */
    private final MemberRegistrationService memberRegistrationService;

    /** The refresh cookie path */
    private final String refreshCookiePath;

    /** The token provider */
    private final JwtTokenProvider tokenProvider;

    /**
     * MemberRegistrationController constructor
     *
     * @param memberRegistrationService
     *            the member registration service
     * @param refreshCookiePath
     *            the refresh cookie path
     * @param tokenProvider
     *            the token provider
     */
    public MemberRegistrationController(MemberRegistrationService memberRegistrationService,
        @Value("${jwt.refresh.cookie.path:/}") String refreshCookiePath, JwtTokenProvider tokenProvider) {
        this.memberRegistrationService = memberRegistrationService;
        this.refreshCookiePath = refreshCookiePath;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Register member.
     *
     * @param newMember
     *            the new member
     * @return response entity
     */
    @Operation(summary = "Register a new member")
    @ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Member registered successfully"),
            @ApiResponse(responseCode = "409", description = "Email already exists"), @ApiResponse(responseCode = "400",
                description = "Invalid input"), @ApiResponse(responseCode = "500",
                    description = "Internal server error") })
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerMember(@Valid @RequestBody RegisterMemberDto newMember) {
        Authentication registeredMember = memberRegistrationService.register(newMember);
        return getTokenCookiesResponseEntity(registeredMember);
    }

    /**
     * Get cookie with token
     *
     * @param authentication
     *            the authentication
     * @return response entity
     */
    private ResponseEntity<Map<String, Object>> getTokenCookiesResponseEntity(Authentication authentication) {
        String accessToken = tokenProvider.generateAccessToken(authentication);
        ResponseCookie accessTokenCookie = ResponseCookie.from("access_token", accessToken).httpOnly(true).path("/")
            .maxAge(tokenProvider.getJwtAccessExpiration()).build();

        String refreshToken = tokenProvider.generateRefreshToken(authentication);
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", refreshToken).httpOnly(true).path(
            refreshCookiePath).maxAge(tokenProvider.getJwtRefreshExpiration()).build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString()).header(
            HttpHeaders.SET_COOKIE, refreshTokenCookie.toString()).body(Map.of("message", "Registration successful",
                "accessTokenExpiry", tokenProvider.getJwtAccessExpiration().toMillis(), "refreshTokenExpiry",
                tokenProvider.getJwtRefreshExpiration().toMillis()));
    }

}
