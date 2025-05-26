package com.kitchensink.api;

import com.kitchensink.dto.MemberDto;
import com.kitchensink.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/member")
public class MemberController{

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @Operation(summary = "Current Member Details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member details retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Member not authenticated or session expired"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/current")
    public ResponseEntity<MemberDto> currentUserData(Authentication authentication) {
        MemberDto memberDto = memberService.currentUserData(authentication);
        return ResponseEntity.ok(memberDto);
    }
}
