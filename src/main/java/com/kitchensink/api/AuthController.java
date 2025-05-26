package com.kitchensink.api;

import com.kitchensink.config.security.JwtTokenProvider;
import com.kitchensink.dto.LoginRequestDto;
import com.kitchensink.dto.RegisterMemberDto;
import com.kitchensink.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
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
public class AuthController{

    private final MemberService memberService;

    private final String refreshCookiePath;

    private final JwtTokenProvider tokenProvider;

    public AuthController(MemberService memberService,
                          @Value("${jwt.refresh.cookie.path:/}") String refreshCookiePath,
                          JwtTokenProvider tokenProvider){
        this.memberService = memberService;
        this.refreshCookiePath = refreshCookiePath;
        this.tokenProvider = tokenProvider;
    }

    @Operation(summary = "Login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member logged in successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequestDto loginRequestDto){
        Authentication login = memberService.login(loginRequestDto);

        return getTokenCookiesResponseEntity(login, "Logged-in successful");
    }

    @Operation(summary = "Refresh")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid refresh token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@CookieValue(value = "refresh_token", required = false) String refreshToken
            , Authentication authentication){
        if(refreshToken == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("message", "Refresh token is missing"));
        }
        tokenProvider.validateRefreshToken(authentication, refreshToken);
        return getTokenCookiesResponseEntity(authentication, "Refreshed successful");
    }

    @Operation(summary = "Register a new member")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Member registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerMember(@Valid @RequestBody RegisterMemberDto newMember){
        Authentication registeredMember = memberService.register(newMember);
        return getTokenCookiesResponseEntity(registeredMember, "Registration successful");
    }

    private ResponseEntity<Map<String, Object>> getTokenCookiesResponseEntity(Authentication authentication,
                                                                              String message){
        String accessToken = tokenProvider.generateAccessToken(authentication);
        ResponseCookie accessTokenCookie = ResponseCookie.from("access_token", accessToken)
                                                         .httpOnly(true).path("/")
                                                         .maxAge(tokenProvider.getJwtAccessExpiration())
                                                         .build();

        String refreshToken = tokenProvider.generateRefreshToken(authentication);
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", refreshToken)
                                                          .httpOnly(true).path(refreshCookiePath)
                                                          .maxAge(tokenProvider.getJwtRefreshExpiration())
                                                          .build();
        return ResponseEntity.ok()
                             .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                             .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                             .body(Map.of("message", message,
                                     "accessTokenExpiry", tokenProvider.getJwtAccessExpiration().toMillis(),
                                     "refreshTokenExpiry", tokenProvider.getJwtRefreshExpiration().toMillis()));
    }

}
