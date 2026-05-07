package com.clubapp.service;

import com.clubapp.dto.request.AttendanceMarkRequest;
import com.clubapp.dto.response.AttendanceResponse;
import com.clubapp.entity.*;
import com.clubapp.exception.BadRequestException;
import com.clubapp.exception.ResourceNotFoundException;
import com.clubapp.exception.UnauthorizedException;
import com.clubapp.repository.AttendanceRepository;
import com.clubapp.repository.EventRepository;
import com.clubapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Transactional
    public AttendanceResponse markAttendance(Long eventId, AttendanceMarkRequest req, User currentUser) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));
        
        if (currentUser.getRole() == Role.COORDINATOR
                && (event.getClub().getCoordinator() == null || !event.getClub().getCoordinator().getId().equals(currentUser.getId()))) {
            throw new UnauthorizedException("You can only mark attendance for your own club's events.");
        }

        // Attendance can only be marked on the event day
        String today = java.time.LocalDate.now().toString(); // YYYY-MM-DD
        if (!event.getDate().equals(today)) {
            throw new BadRequestException("Attendance can only be marked on the event day: " + event.getDate());
        }

        User target = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + req.getUserId()));
        
        Attendance att = attendanceRepository.findByEventAndUser(event, target)
                .orElse(Attendance.builder().event(event).user(target).build());
        
        att.setStatus(req.getStatus());
        att.setUtterance(req.getUtterance());
        return mapToResponse(attendanceRepository.save(att));
    }

    public List<AttendanceResponse> getAttendanceByEvent(Long eventId, User currentUser) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));
        if (currentUser.getRole() == Role.COORDINATOR
                && (event.getClub().getCoordinator() == null || !event.getClub().getCoordinator().getId().equals(currentUser.getId())))
            throw new UnauthorizedException("Access denied.");
        return attendanceRepository.findByEvent(event).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private AttendanceResponse mapToResponse(Attendance a) {
        return AttendanceResponse.builder()
                .id(a.getId())
                .userId(a.getUser().getId()).userName(a.getUser().getName())
                .email(a.getUser().getEmail()).status(a.getStatus())
                .utterance(a.getUtterance())
                .eventId(a.getEvent().getId()).eventName(a.getEvent().getName()).build();
    }
}
