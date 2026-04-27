package com.clubapp.controller;

import com.clubapp.dto.request.AttendanceMarkRequest;
import com.clubapp.dto.response.AttendanceResponse;
import com.clubapp.entity.User;
import com.clubapp.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.io.IOException;
import com.clubapp.service.AttendanceExportService;

import java.util.List;

/**
 * Replaces: clubInteractionMenu cases 8 (Mark Attendance) and 9 (View Attendance)
 *
 * POST /api/events/{id}/attendance — ADMIN or COORDINATOR
 * GET  /api/events/{id}/attendance — ADMIN or COORDINATOR
 */
@RestController
@RequestMapping("/api/events/{eventId}/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AttendanceExportService attendanceExportService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    public ResponseEntity<AttendanceResponse> markAttendance(
            @PathVariable Long eventId,
            @Valid @RequestBody AttendanceMarkRequest req,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(attendanceService.markAttendance(eventId, req, currentUser));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    public ResponseEntity<List<AttendanceResponse>> getAttendance(
            @PathVariable Long eventId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(attendanceService.getAttendanceByEvent(eventId, currentUser));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'COORDINATOR')")
    public ResponseEntity<byte[]> exportAttendance(
            @PathVariable Long eventId,
            @AuthenticationPrincipal User currentUser) throws IOException {
        byte[] excelData = attendanceExportService.generateAttendanceExcel(eventId, currentUser);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "attendance_event_" + eventId + ".xlsx");
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }
}
