package com.clubapp.service;

import com.clubapp.dto.request.CreateEventRequest;
import com.clubapp.dto.response.EventResponse;
import com.clubapp.dto.response.UserResponse;
import com.clubapp.entity.*;
import com.clubapp.exception.ResourceNotFoundException;
import com.clubapp.repository.ClubRepository;
import com.clubapp.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ClubRepository clubRepository;

    @Transactional
    public EventResponse createEvent(Long clubId, CreateEventRequest req, User currentUser) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found: " + clubId));
        if (currentUser.getRole() == Role.COORDINATOR
                && !club.getCoordinator().getId().equals(currentUser.getId()))
            throw new IllegalArgumentException("You can only create events for your own club.");
        Event event = Event.builder().name(req.getName()).description(req.getDescription())
                .venue(req.getVenue()).date(req.getDate()).time(req.getTime())
                .membersOnly(req.isMembersOnly()).maxAttendees(req.getMaxAttendees())
                .club(club).build();
        return mapToResponse(eventRepository.save(event), currentUser);
    }

    public List<EventResponse> getEventsByClub(Long clubId, User currentUser) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found: " + clubId));
        return club.getEvents().stream().map(e -> mapToResponse(e, currentUser)).collect(Collectors.toList());
    }

    @Transactional
    public EventResponse updateEvent(Long eventId, CreateEventRequest req, User currentUser) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));
        if (currentUser.getRole() == Role.COORDINATOR
                && !event.getClub().getCoordinator().getId().equals(currentUser.getId()))
            throw new IllegalArgumentException("You can only update your own club's events.");
        event.setName(req.getName()); event.setDescription(req.getDescription());
        event.setVenue(req.getVenue()); event.setDate(req.getDate());
        event.setTime(req.getTime()); event.setMembersOnly(req.isMembersOnly());
        event.setMaxAttendees(req.getMaxAttendees());
        return mapToResponse(eventRepository.save(event), currentUser);
    }

    @Transactional
    public void deleteEvent(Long eventId, User currentUser) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));
        if (currentUser.getRole() == Role.COORDINATOR
                && !event.getClub().getCoordinator().getId().equals(currentUser.getId()))
            throw new IllegalArgumentException("You can only delete your own club's events.");
        eventRepository.delete(event);
    }

    @Transactional
    public EventResponse registerForEvent(Long eventId, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN)
            throw new IllegalArgumentException("Admin cannot register for events.");
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));
        if (event.isMembersOnly()) {
            boolean isMember = event.getClub().getMembers().stream()
                    .anyMatch(m -> m.getId().equals(currentUser.getId()));
            if (!isMember)
                throw new IllegalArgumentException("This event is for club members only.");
        }
        if (event.getAttendees().stream().anyMatch(a -> a.getId().equals(currentUser.getId())))
            throw new IllegalArgumentException("You are already registered for this event.");
        
        if (event.getMaxAttendees() != null && event.getAttendees().size() >= event.getMaxAttendees())
            throw new IllegalArgumentException("This event is full.");

        event.getAttendees().add(currentUser);
        return mapToResponse(eventRepository.save(event), currentUser);
    }

    @Transactional
    public void updatePosterImage(Long eventId, String filename) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));
        event.setPosterImage(filename);
        eventRepository.save(event);
    }

    public List<EventResponse> getOngoingEvents(User currentUser) {
        String today = LocalDate.now().toString();
        return eventRepository.findAll().stream()
                .filter(e -> e.getDate() != null && e.getDate().compareTo(today) >= 0)
                .map(e -> mapToResponse(e, currentUser)).collect(Collectors.toList());
    }

    public EventResponse getEventById(Long eventId, User currentUser) {
        return mapToResponse(eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId)), currentUser);
    }

    public EventResponse mapToResponse(Event event, User currentUser) {
        boolean isRegistered = currentUser != null && event.getAttendees().stream()
                .anyMatch(a -> a.getId().equals(currentUser.getId()));
        boolean isFull = event.getMaxAttendees() != null && event.getAttendees().size() >= event.getMaxAttendees();
        return EventResponse.builder()
                .id(event.getId()).name(event.getName()).description(event.getDescription())
                .venue(event.getVenue()).date(event.getDate()).time(event.getTime())
                .posterImage(event.getPosterImage()).membersOnly(event.isMembersOnly())
                .maxAttendees(event.getMaxAttendees())
                .clubId(event.getClub().getId()).clubName(event.getClub().getName())
                .attendeeCount(event.getAttendees().size()).isRegistered(isRegistered)
                .isFull(isFull).build();
    }

    public List<UserResponse> getEventAttendees(Long eventId, User currentUser) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));
        if (currentUser.getRole() == Role.COORDINATOR
                && !event.getClub().getCoordinator().getId().equals(currentUser.getId()))
            throw new IllegalArgumentException("Access denied.");
        
        return event.getAttendees().stream().map(u -> UserResponse.builder()
                .id(u.getId()).name(u.getName()).email(u.getEmail())
                .department(u.getDepartment()).year(u.getYear()).build()
        ).collect(Collectors.toList());
    }
}
