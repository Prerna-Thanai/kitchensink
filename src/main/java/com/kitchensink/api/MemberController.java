package com.kitchensink.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kitchensink.dto.MemberDto;
import com.kitchensink.dto.UpdateMemberRequest;
import com.kitchensink.service.MemberService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping(value = "/api/member")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @Operation(summary = "Current Member Details")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Member details retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Member not authenticated or session expired"),
            @ApiResponse(responseCode = "500", description = "Internal server error") })
    @GetMapping("/current")
    public ResponseEntity<MemberDto> currentUserData(Authentication authentication) {
        MemberDto memberDto = memberService.currentUserData(authentication);
        return ResponseEntity.ok(memberDto);
    }

    @GetMapping("/all")
    public ResponseEntity<PagedModel<MemberDto>> getAllMembers(@PageableDefault(sort = { "id" }) Pageable pageable,
        @RequestParam(value = "showInactiveMembers", required = false) boolean showInactiveMembers) {
        Page<MemberDto> members = memberService.getAllMembers(pageable, showInactiveMembers);
        return ResponseEntity.ok(new PagedModel<>(members));
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<String> deleteUserById(@PathVariable String memberId) {
        memberService.deleteMemberById(memberId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{memberId}")
    public ResponseEntity<MemberDto> updateUserById(@PathVariable String memberId,
        @RequestBody UpdateMemberRequest updateRequest) {
        MemberDto updatedMember = memberService.updateMemberDetails(memberId, updateRequest);
        return ResponseEntity.ok().body(updatedMember);
    }

}
