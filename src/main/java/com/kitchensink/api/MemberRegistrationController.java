package com.kitchensink.api;

import com.kitchensink.dto.RegisterMemberDto;
import com.kitchensink.service.MemberRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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

    /**
     * MemberRegistrationController constructor
     *
     * @param memberRegistrationService
     *            the member registration service
     */
    public MemberRegistrationController(MemberRegistrationService memberRegistrationService) {
        this.memberRegistrationService = memberRegistrationService;
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
        memberRegistrationService.register(newMember);
        return ResponseEntity.ok().body(Map.of("message", "Registration successful"));
    }

}
