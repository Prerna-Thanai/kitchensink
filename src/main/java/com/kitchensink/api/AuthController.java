package com.kitchensink.api;

import com.kitchensink.config.security.JwtTokenProvider;
import com.kitchensink.dto.LoginRequestDto;
import com.kitchensink.service.LoginService;
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
 * The Class AuthController.
 *
 * @author prerna
 */
@RestController
@RequestMapping(value = "/api/auth")
public class AuthController {

    /** The login service */
    private final LoginService loginService;

    /** The refresh cookie path */
    private final String refreshCookiePath;

    /** The token provider */
    private final JwtTokenProvider tokenProvider;

    /**
     * AuthController constructor
     *
     * @param loginService
     *            the login service
     * @param refreshCookiePath
     *            the refresh cookie path
     * @param tokenProvider
     *            the token provider
     */
    public AuthController(LoginService loginService, @Value("${jwt.refresh.cookie.path:/}") String refreshCookiePath,
        JwtTokenProvider tokenProvider) {
        this.loginService = loginService;
        this.refreshCookiePath = refreshCookiePath;
        this.tokenProvider = tokenProvider;
    }

    /**
     * Login member
     *
     * @param loginRequestDto
     *            the login request dto
     * @return response entity
     */
    @Operation(summary = "Login")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Member logged in successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password"), @ApiResponse(
                responseCode = "500", description = "Internal server error") })
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        Authentication login = loginService.login(loginRequestDto);

        return getTokenCookiesResponseEntity(login);
    }

    /**
     * Get Cookie with token
     *
     * @param authentication the authentication
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
            HttpHeaders.SET_COOKIE, refreshTokenCookie.toString()).body(Map.of("message", "Logged-in successful", "accessTokenExpiry",
                tokenProvider.getJwtAccessExpiration().toMillis(), "refreshTokenExpiry", tokenProvider
                    .getJwtRefreshExpiration().toMillis()));
    }

    /**
     * Logout member
     *
     * @return response entity
     */
    @Operation(summary = "Logout")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Member logged out successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password") })
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        return getTokenRemovalCookiesResponseEntity();
    }

    /**
     * Get Cookie with token removal
     *
     * @return response entity
     */
    public ResponseEntity<Map<String, Object>> getTokenRemovalCookiesResponseEntity() {
        ResponseCookie clearAccessToken = ResponseCookie.from("access_token", "").httpOnly(true).path("/").maxAge(0)
            .build();

        ResponseCookie clearRefreshToken = ResponseCookie.from("refresh_token", "").httpOnly(true).path(
            refreshCookiePath).maxAge(0).build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, clearAccessToken.toString()).header(
            HttpHeaders.SET_COOKIE, clearRefreshToken.toString()).body(Map.of("message", "Logged out successfully"));
    }

}
