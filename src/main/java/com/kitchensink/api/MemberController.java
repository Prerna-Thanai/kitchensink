package com.kitchensink.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kitchensink.dto.MemberDto;
import com.kitchensink.dto.MemberSearchCriteria;
import com.kitchensink.dto.UpdateMemberRequest;
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
@RequestMapping(value = "/api/members")
public class MemberController {

    /** The member service */
    private final MemberService memberService;

    /**
     * Member controller constructor
     *
     * @param memberService
     */
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * Gets the current member details.
     *
     * @param authentication
     *            the authentication
     * @return the member dto
     */
    @Operation(summary = "Current Member Details")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Member details retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Member not authenticated or session expired"),
            @ApiResponse(responseCode = "500", description = "Internal server error") })
    @GetMapping("/current")
    public ResponseEntity<MemberDto> currentUserData(Authentication authentication) {
        MemberDto memberDto = memberService.currentUserData(authentication);
        return ResponseEntity.ok(memberDto);
    }

    /**
     * Gets all member details.
     *
     * @param pageable
     *            the pageable
     * @param showInactiveMembers
     *            the show inactive members
     * @return the member dto
     */
    @Operation(summary = "Get all members")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Members list recieved successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password"), @ApiResponse(
                responseCode = "500", description = "Internal server error") })
    @GetMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedModel<MemberDto>> getAllMembers(@PageableDefault(sort = { "id" }) Pageable pageable,
        @RequestParam(value = "showInactiveMembers", required = false) boolean showInactiveMembers) {
        Page<MemberDto> members = memberService.getAllMembers(pageable, showInactiveMembers);
        return ResponseEntity.ok(new PagedModel<>(members));
    }

    /**
     * Delete member by ID.
     *
     * @param id
     *            the id of the member
     * @return the response entity
     */
    @Operation(summary = "Delete Member by ID", description = "Delete a member using their ID.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Member deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Member not found") })
    @DeleteMapping("/{memberId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUserById(@PathVariable String memberId) {
        memberService.deleteMemberById(memberId);
        return ResponseEntity.ok().build();
    }

    /**
     * Update member by ID.
     *
     * @param memberId
     *            the member id
     * @param updateRequest
     *            the update request
     * @return response entity member dto
     */
    @Operation(summary = "Update Member by ID", description = "Update member details using member id.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Member updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"), @ApiResponse(responseCode = "404",
                description = "Member not found") })
    @PutMapping("/{memberId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MemberDto> updateUserById(@PathVariable String memberId,
        @RequestBody UpdateMemberRequest updateRequest) {
        MemberDto updatedMember = memberService.updateMemberDetails(memberId, updateRequest);
        return ResponseEntity.ok().body(updatedMember);
    }

    @Operation(summary = "Filter members by Criteria")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Members list recieved successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password"), @ApiResponse(
                responseCode = "500", description = "Internal server error") })
    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedModel<MemberDto>> getFilteredMembersByCriteria(@PageableDefault(sort = {
            "id" }) Pageable pageable, @RequestParam(value = "showInactiveMembers",
                required = false) boolean showInactiveMembers, @RequestBody MemberSearchCriteria searchCriteria) {
        Page<MemberDto> members = memberService.getFilteredMembersByCriteria(pageable, showInactiveMembers,
            searchCriteria);
        return ResponseEntity.ok(new PagedModel<>(members));
    }

}
