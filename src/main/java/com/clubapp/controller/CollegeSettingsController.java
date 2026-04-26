package com.clubapp.controller;

import com.clubapp.dto.request.CollegeDetailsRequest;
import com.clubapp.dto.response.CollegeDetailsResponse;
import com.clubapp.service.CollegeDetailsService;
import com.clubapp.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/college")
@RequiredArgsConstructor
public class CollegeSettingsController {

    private final CollegeDetailsService collegeDetailsService;
    private final FileUploadService fileUploadService;

    // Public — landing page reads this
    @GetMapping
    public ResponseEntity<CollegeDetailsResponse> getDetails() {
        return ResponseEntity.ok(collegeDetailsService.getDetails());
    }

    // Admin only — save/update college details
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CollegeDetailsResponse> saveDetails(@RequestBody CollegeDetailsRequest req) {
        return ResponseEntity.ok(collegeDetailsService.saveDetails(req));
    }

    // Admin only — upload college logo
    @PostMapping("/logo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CollegeDetailsResponse> uploadLogo(
            @RequestParam("file") MultipartFile file) throws IOException {
        String filename = fileUploadService.uploadFile(file, "college");
        return ResponseEntity.ok(collegeDetailsService.updateLogo(filename));
    }
}
