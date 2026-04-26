package com.clubapp.controller;

import com.clubapp.dto.request.CreateCoordinatorRequest;
import com.clubapp.dto.response.*;
import com.clubapp.service.AdminService;
import com.clubapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverview() {
        return ResponseEntity.ok(adminService.getOverview());
    }

    @GetMapping("/clubs")
    public ResponseEntity<List<ClubResponse>> getAllClubs() {
        return ResponseEntity.ok(adminService.getAllClubs());
    }

    @GetMapping("/clubs/{id}")
    public ResponseEntity<ClubResponse> getClub(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getClubById(id));
    }

    @GetMapping("/clubs/{id}/events/ongoing")
    public ResponseEntity<List<EventResponse>> getOngoing(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getOngoingEventsByClub(id));
    }

    @GetMapping("/clubs/{id}/events/past")
    public ResponseEntity<List<EventResponse>> getPast(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getPastEventsByClub(id));
    }

    @GetMapping("/clubs/{id}/members")
    public ResponseEntity<List<UserResponse>> getMembers(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getClubMembers(id));
    }

    @PutMapping("/clubs/{id}/coordinator")
    public ResponseEntity<ClubResponse> updateCoordinator(@PathVariable Long id,
                                                           @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(adminService.updateCoordinator(id, body.get("email")));
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getEventById(id));
    }

    @GetMapping("/events/{id}/attendees")
    public ResponseEntity<List<UserResponse>> getAttendees(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getEventAttendees(id));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/users/coordinators")
    public ResponseEntity<List<UserResponse>> getCoordinators() {
        return ResponseEntity.ok(userService.getAllCoordinators());
    }

    @PostMapping("/users/coordinator")
    public ResponseEntity<UserResponse> createCoordinator(@Valid @RequestBody CreateCoordinatorRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createCoordinator(req));
    }

    @PutMapping("/users/{id}/promote")
    public ResponseEntity<UserResponse> promote(@PathVariable Long id) {
        return ResponseEntity.ok(userService.promoteToCoordinator(id));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
