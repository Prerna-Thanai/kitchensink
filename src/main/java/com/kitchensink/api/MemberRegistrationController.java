package com.kitchensink.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kitchensink.dto.RegisterMemberDto;
import com.kitchensink.service.MemberRegistrationService;
import com.kitchensink.service.MemberService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * The Class MemberController.
 *
 * @author prerna
 */
@RestController
@RequestMapping(value = "/api/member/register")
public class MemberRegistrationController {

    @Autowired
    private MemberRegistrationService memberRegistrationService;

    @Autowired
    private MemberService memberService;

    @Operation(summary = "Register a new member")
    @ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Member registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"), @ApiResponse(responseCode = "500",
                description = "Internal server error") })
    @PostMapping("/")
    public ResponseEntity<String> registerMember(@RequestBody RegisterMemberDto newMember) throws Exception {
        memberRegistrationService.register(newMember);
        return ResponseEntity.ok("Registration successful");
        // } catch (Exception e) {
        // return ResponseEntity
        // .internalServerError()
        // .body(getRootErrorMessage(e));
        // }
    }

    // TODO do exception handling
    // Extract root error message (can also be moved to a utility class)
    private String getRootErrorMessage(Exception e) {
        String errorMessage = "Registration failed. See server log for more information";
        if (e == null) {
            return errorMessage;
        }
        Throwable t = e;
        while (t != null) {
            errorMessage = t.getLocalizedMessage();
            t = t.getCause();
        }
        return errorMessage;
    }

}
