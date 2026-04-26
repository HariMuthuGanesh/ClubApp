package com.clubapp.controller;

import com.clubapp.dto.request.JoinRequestRequest;
import com.clubapp.dto.response.JoinRequestResponse;
import com.clubapp.dto.response.UserResponse;
import com.clubapp.entity.User;
import com.clubapp.service.ClubJoinRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
public class ClubJoinRequestController {

    private final ClubJoinRequestService joinRequestService;

    @PostMapping("/{clubId}/join-request")
    public ResponseEntity<JoinRequestResponse> sendRequest(@PathVariable Long clubId,
                                                            @RequestBody JoinRequestRequest req,
                                                            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(joinRequestService.sendRequest(clubId, currentUser, req.getMessage()));
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<JoinRequestResponse>> getMyRequests(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(joinRequestService.getMyRequests(currentUser));
    }

    @GetMapping("/requests/pending")
    @PreAuthorize("hasRole('COORDINATOR')")
    public ResponseEntity<List<JoinRequestResponse>> getPending(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(joinRequestService.getPendingRequests(currentUser));
    }

    @GetMapping("/requests/all")
    @PreAuthorize("hasRole('COORDINATOR')")
    public ResponseEntity<List<JoinRequestResponse>> getAll(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(joinRequestService.getAllRequestsForClub(currentUser));
    }

    @PutMapping("/requests/{id}/accept")
    @PreAuthorize("hasAnyRole('COORDINATOR','ADMIN')")
    public ResponseEntity<JoinRequestResponse> accept(@PathVariable Long id,
                                                       @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(joinRequestService.acceptRequest(id, currentUser));
    }

    @PutMapping("/requests/{id}/reject")
    @PreAuthorize("hasAnyRole('COORDINATOR','ADMIN')")
    public ResponseEntity<JoinRequestResponse> reject(@PathVariable Long id,
                                                       @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(joinRequestService.rejectRequest(id, currentUser));
    }

    @GetMapping("/my-club/members")
    @PreAuthorize("hasRole('COORDINATOR')")
    public ResponseEntity<List<UserResponse>> getMembers(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(joinRequestService.getClubMembers(currentUser));
    }

    @PutMapping("/my-club/members/{userId}/promote")
    @PreAuthorize("hasRole('COORDINATOR')")
    public ResponseEntity<UserResponse> promote(@PathVariable Long userId,
                                                 @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(joinRequestService.promoteToCoordinator(userId, currentUser));
    }
}
