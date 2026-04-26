package com.clubapp.service;

import com.clubapp.dto.response.*;
import com.clubapp.entity.*;
import com.clubapp.exception.ResourceNotFoundException;
import com.clubapp.repository.ClubRepository;
import com.clubapp.repository.EventRepository;
import com.clubapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ClubRepository clubRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ClubService clubService;

    public Map<String, Object> getOverview() {
        String today = LocalDate.now().toString();
        List<EventResponse> ongoingEvents = eventRepository.findAll().stream()
                .filter(e -> e.getDate() != null && e.getDate().compareTo(today) >= 0)
                .map(this::mapEvent).collect(Collectors.toList());
        Map<String, Object> ov = new HashMap<>();
        ov.put("totalClubs",   clubRepository.count());
        ov.put("totalUsers",   userRepository.count());
        ov.put("totalEvents",  eventRepository.count());
        ov.put("ongoingCount", ongoingEvents.size());
        ov.put("ongoingEvents", ongoingEvents);
        return ov;
    }

    public List<ClubResponse> getAllClubs() {
        return clubRepository.findAll().stream()
                .map(c -> clubService.mapToResponse(c, null)).collect(Collectors.toList());
    }

    public ClubResponse getClubById(Long id) {
        return clubService.mapToResponse(clubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found: " + id)), null);
    }

    public List<EventResponse> getOngoingEventsByClub(Long clubId) {
        String today = LocalDate.now().toString();
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found: " + clubId));
        return club.getEvents().stream()
                .filter(e -> e.getDate() != null && e.getDate().compareTo(today) >= 0)
                .map(this::mapEvent).collect(Collectors.toList());
    }

    public List<EventResponse> getPastEventsByClub(Long clubId) {
        String today = LocalDate.now().toString();
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found: " + clubId));
        return club.getEvents().stream()
                .filter(e -> e.getDate() != null && e.getDate().compareTo(today) < 0)
                .map(this::mapEvent).collect(Collectors.toList());
    }

    public List<UserResponse> getClubMembers(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found: " + clubId));
        return club.getMembers().stream().map(userService::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public ClubResponse updateCoordinator(Long clubId, String coordinatorEmail) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found: " + clubId));
        User newCoord = userRepository.findByEmail(coordinatorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + coordinatorEmail));
        if (newCoord.getRole() != Role.COORDINATOR) {
            newCoord.setRole(Role.COORDINATOR);
            userRepository.save(newCoord);
        }
        club.setCoordinator(newCoord);
        return clubService.mapToResponse(clubRepository.save(club), null);
    }

    public EventResponse getEventById(Long eventId) {
        return eventRepository.findById(eventId).map(this::mapEvent)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));
    }

    public List<UserResponse> getEventAttendees(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));
        return event.getAttendees().stream().map(userService::mapToResponse).collect(Collectors.toList());
    }

    private EventResponse mapEvent(Event e) {
        return EventResponse.builder()
                .id(e.getId()).name(e.getName()).description(e.getDescription())
                .venue(e.getVenue()).date(e.getDate()).time(e.getTime())
                .posterImage(e.getPosterImage()).membersOnly(e.isMembersOnly())
                .clubId(e.getClub().getId()).clubName(e.getClub().getName())
                .attendeeCount(e.getAttendees().size()).build();
    }
}
