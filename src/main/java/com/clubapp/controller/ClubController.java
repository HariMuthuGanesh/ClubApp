package com.clubapp.controller;

import com.clubapp.dto.request.CreateClubRequest;
import com.clubapp.dto.response.ClubResponse;
import com.clubapp.entity.User;
import com.clubapp.service.ClubService;
import com.clubapp.service.FileUploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;
    private final FileUploadService fileUploadService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClubResponse> createClub(@Valid @RequestBody CreateClubRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clubService.createClub(req));
    }

    @GetMapping
    public ResponseEntity<List<ClubResponse>> getAllClubs(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(clubService.getAllClubs(currentUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClubResponse> getClubById(@PathVariable Long id,
                                                     @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(clubService.getClubById(id, currentUser));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ClubResponse>> getMyClubs(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(clubService.getMyClubs(currentUser));
    }

    @GetMapping("/my-club")
    @PreAuthorize("hasRole('COORDINATOR')")
    public ResponseEntity<?> getMyAssignedClub(@AuthenticationPrincipal User currentUser) {
        return clubService.getClubByCoordinator(currentUser)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(Map.of("message", "No club assigned to you yet.")));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR')")
    public ResponseEntity<ClubResponse> updateClub(@PathVariable Long id,
                                                    @RequestBody CreateClubRequest req,
                                                    @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(clubService.updateClubInfo(id, req, currentUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteClub(@PathVariable Long id) {
        clubService.deleteClub(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/logo")
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR')")
    public ResponseEntity<ClubResponse> uploadLogo(@PathVariable Long id,
                                                    @RequestParam("file") MultipartFile file,
                                                    @AuthenticationPrincipal User currentUser) throws IOException {
        String filename = fileUploadService.uploadFile(file, "clubs");
        clubService.updateLogoImage(id, filename);
        return ResponseEntity.ok(clubService.getClubById(id, currentUser));
    }
}
