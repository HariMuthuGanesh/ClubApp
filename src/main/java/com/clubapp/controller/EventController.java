package com.clubapp.controller;

import com.clubapp.dto.request.CreateEventRequest;
import com.clubapp.dto.response.EventResponse;
import com.clubapp.dto.response.UserResponse;
import com.clubapp.entity.User;
import com.clubapp.service.EventService;
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
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final FileUploadService fileUploadService;

    @PostMapping("/api/clubs/{clubId}/events")
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR')")
    public ResponseEntity<EventResponse> createEvent(@PathVariable Long clubId,
                                                      @Valid @RequestBody CreateEventRequest req,
                                                      @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.createEvent(clubId, req, currentUser));
    }

    @GetMapping("/api/clubs/{clubId}/events")
    public ResponseEntity<List<EventResponse>> getEventsByClub(@PathVariable Long clubId,
                                                                @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(eventService.getEventsByClub(clubId, currentUser));
    }

    @GetMapping("/api/clubs/{clubId}/events/ongoing")
    public ResponseEntity<List<EventResponse>> getOngoingEventsByClub(@PathVariable Long clubId,
                                                                      @AuthenticationPrincipal User currentUser) {
        String today = java.time.LocalDate.now().toString();
        String fiveDaysAgo = java.time.LocalDate.now().minusDays(5).toString();
        List<EventResponse> ongoing = eventService.getEventsByClub(clubId, currentUser).stream()
                .filter(e -> e.getDate() != null && e.getDate().compareTo(fiveDaysAgo) >= 0)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(ongoing);
    }

    @GetMapping("/api/events/my-participation")
    public ResponseEntity<List<EventResponse>> getMyParticipatedEvents(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(eventService.getMyParticipatedEvents(currentUser));
    }

    @GetMapping("/api/events/ongoing")
    public ResponseEntity<List<EventResponse>> getOngoingEvents(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(eventService.getOngoingEvents(currentUser));
    }

    @GetMapping("/api/events/{id}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable Long id,
                                                   @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(eventService.getEventById(id, currentUser));
    }

    @PutMapping("/api/events/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR')")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable Long id,
                                                      @Valid @RequestBody CreateEventRequest req,
                                                      @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(eventService.updateEvent(id, req, currentUser));
    }

    @DeleteMapping("/api/events/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR')")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id,
                                             @AuthenticationPrincipal User currentUser) {
        eventService.deleteEvent(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/events/{id}/register")
    public ResponseEntity<EventResponse> registerForEvent(@PathVariable Long id,
                                                           @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(eventService.registerForEvent(id, currentUser));
    }

    @PostMapping("/api/events/{id}/poster")
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR')")
    public ResponseEntity<Void> uploadPoster(@PathVariable Long id,
                                              @RequestParam("file") MultipartFile file) throws IOException {
        String filename = fileUploadService.uploadFile(file, "events");
        eventService.updatePosterImage(id, filename);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/events/{id}/attendees")
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR')")
    public ResponseEntity<List<UserResponse>> getEventAttendees(@PathVariable Long id,
                                                                 @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(eventService.getEventAttendees(id, currentUser));
    }
}
