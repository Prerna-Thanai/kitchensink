package com.kitchensink.api;

import com.kitchensink.config.security.JwtTokenProvider;
import com.kitchensink.dto.RegisterMemberDto;
import com.kitchensink.service.MemberRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * The Class MemberController.
 *
 * @author prerna
 */
@RestController
@RequestMapping(value = "/api/auth")
public class MemberRegistrationController {

    private final MemberRegistrationService memberRegistrationService;

    private final String refreshCookiePath;

    private final JwtTokenProvider tokenProvider;

    public MemberRegistrationController(MemberRegistrationService memberRegistrationService,
        @Value("${jwt.refresh.cookie.path:/}") String refreshCookiePath, JwtTokenProvider tokenProvider) {
        this.memberRegistrationService = memberRegistrationService;
        this.refreshCookiePath = refreshCookiePath;
        this.tokenProvider = tokenProvider;
    }

    @Operation(summary = "Register a new member")
    @ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Member registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"), @ApiResponse(responseCode = "500",
                description = "Internal server error") })
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerMember(@Valid @RequestBody RegisterMemberDto newMember) {
        Authentication registeredMember = memberRegistrationService.register(newMember);
        return getTokenCookiesResponseEntity(registeredMember);
    }

    private ResponseEntity<Map<String, Object>> getTokenCookiesResponseEntity(Authentication authentication) {
        String accessToken = tokenProvider.generateAccessToken(authentication);
        ResponseCookie accessTokenCookie = ResponseCookie.from("access_token", accessToken).httpOnly(true).path("/")
                                                         .sameSite("None").maxAge(tokenProvider.getJwtAccessExpiration()).build();

        String refreshToken = tokenProvider.generateRefreshToken(authentication);
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", refreshToken).httpOnly(true).path(
            refreshCookiePath).sameSite("None").maxAge(tokenProvider.getJwtRefreshExpiration()).build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString()).header(
            HttpHeaders.SET_COOKIE, refreshTokenCookie.toString()).body(Map.of("message", "Registration successful", "accessTokenExpiry",
                tokenProvider.getJwtAccessExpiration().toMillis(), "refreshTokenExpiry", tokenProvider
                    .getJwtRefreshExpiration().toMillis()));
    }

}
