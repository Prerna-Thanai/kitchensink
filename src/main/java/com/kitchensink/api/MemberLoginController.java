package com.kitchensink.api;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kitchensink.dto.LoginRequest;
import com.kitchensink.service.LoginService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * The Class MemberController.
 *
 * @author prerna
 */
@RestController
@RequestMapping(value = "/api/member/login")
public class MemberLoginController {

    @Autowired
    private LoginService loginService;

    // TODO add apikey validation on login
    @Operation(summary = "Login")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "User logged in successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid username and password"), @ApiResponse(responseCode = "500",
            description = "Internal server error")})
    @PostMapping("/")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) throws Exception {
        String token = loginService.login(loginRequest);

        ResponseCookie cookie = ResponseCookie.from("jwt", token).httpOnly(true)   // .secure(true) // set true for
                // HTTPS
                .path("/").maxAge(Duration.ofDays(1)).build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body("Login successful");
    }

}
