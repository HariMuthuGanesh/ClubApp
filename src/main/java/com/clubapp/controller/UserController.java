package com.clubapp.controller;

import com.clubapp.dto.request.ChangePasswordRequest;
import com.clubapp.dto.request.UpdateProfileRequest;
import com.clubapp.dto.response.MessageResponse;
import com.clubapp.dto.response.UserResponse;
import com.clubapp.entity.User;
import com.clubapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Get own profile
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.mapToResponse(currentUser));
    }

    // Update own profile
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestBody UpdateProfileRequest req,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.updateProfile(currentUser, req));
    }

    // Change password
    @PutMapping("/me/password")
    public ResponseEntity<MessageResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest req,
            @AuthenticationPrincipal User currentUser) {
        userService.changePassword(currentUser, req);
        return ResponseEntity.ok(new MessageResponse("Password changed successfully."));
    }

    // Admin: get all users
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // Admin: delete user
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
