package com.clubapp.service;

import com.clubapp.dto.request.CreateEventRequest;
import com.clubapp.dto.response.EventResponse;
import com.clubapp.dto.response.UserResponse;
import com.clubapp.entity.*;
import com.clubapp.exception.BadRequestException;
import com.clubapp.exception.ConflictException;
import com.clubapp.exception.ResourceNotFoundException;
import com.clubapp.exception.UnauthorizedException;
import com.clubapp.repository.AttendanceRepository;
import com.clubapp.repository.ClubRepository;
import com.clubapp.repository.EventRepository;
import com.clubapp.repository.NewsRepository;
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
    private final AttendanceRepository attendanceRepository;
    private final NewsRepository newsRepository;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @Transactional
    public EventResponse createEvent(Long clubId, CreateEventRequest req, User currentUser) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found: " + clubId));
        if (currentUser.getRole() == Role.COORDINATOR
                && (club.getCoordinator() == null || !club.getCoordinator().getId().equals(currentUser.getId())))
            throw new UnauthorizedException("You can only create events for your own club.");
        Event event = Event.builder().name(req.getName()).description(req.getDescription())
                .venue(req.getVenue()).date(req.getDate()).time(req.getTime())
                .membersOnly(req.isMembersOnly()).maxAttendees(req.getMaxAttendees())
                .club(club).build();
        EventResponse response = mapToResponse(eventRepository.save(event), currentUser);
        messagingTemplate.convertAndSend("/topic/events", response);
        return response;
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
                && (event.getClub().getCoordinator() == null || !event.getClub().getCoordinator().getId().equals(currentUser.getId())))
            throw new UnauthorizedException("You can only update your own club's events.");
        event.setName(req.getName()); event.setDescription(req.getDescription());
        event.setVenue(req.getVenue()); event.setDate(req.getDate());
        event.setTime(req.getTime()); event.setMembersOnly(req.isMembersOnly());
        event.setMaxAttendees(req.getMaxAttendees());
        
        String oldWinners = event.getWinners();
        event.setWinners(req.getWinners());
        
        Event saved = eventRepository.save(event);
        
        // Post news if winners were added/changed
        if (req.getWinners() != null && !req.getWinners().isEmpty() && !req.getWinners().equals(oldWinners)) {
            News news = News.builder()
                    .title("🏆 Winners Announced: " + saved.getName())
                    .content("The results for the event '" + saved.getName() + "' are out! \nWinners: " + req.getWinners())
                    .club(saved.getClub())
                    .build();
            newsRepository.save(news);
        }
        
        EventResponse response = mapToResponse(saved, currentUser);
        messagingTemplate.convertAndSend("/topic/events", response);
        return response;
    }

    @Transactional
    public void deleteEvent(Long eventId, User currentUser) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));
        if (currentUser.getRole() == Role.COORDINATOR
                && (event.getClub().getCoordinator() == null || !event.getClub().getCoordinator().getId().equals(currentUser.getId())))
            throw new UnauthorizedException("You can only delete your own club's events.");
        
        attendanceRepository.deleteByEvent(event);
        eventRepository.delete(event);
    }

    @Transactional
    public EventResponse registerForEvent(Long eventId, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN)
            throw new BadRequestException("Admin cannot register for events.");
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));
        if (event.isMembersOnly()) {
            boolean isMember = event.getClub().getMembers().stream()
                    .anyMatch(m -> m.getId().equals(currentUser.getId()));
            if (!isMember)
                throw new UnauthorizedException("This event is for club members only.");
        }
        if (event.getAttendees().stream().anyMatch(a -> a.getId().equals(currentUser.getId())))
            throw new ConflictException("You are already registered for this event.");
        
        if (event.getMaxAttendees() != null && event.getAttendees().size() >= event.getMaxAttendees())
            throw new BadRequestException("This event is full.");
        
        String today = LocalDate.now().toString();
        if (event.getDate() != null && event.getDate().compareTo(today) < 0)
            throw new BadRequestException("Cannot register for a past event.");

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

    public List<EventResponse> getMyParticipatedEvents(User currentUser) {
        return eventRepository.findByAttendeesContaining(currentUser).stream()
                .map(e -> mapToResponse(e, currentUser))
                .collect(Collectors.toList());
    }

    public List<EventResponse> getOngoingEvents(User currentUser) {
        String today = LocalDate.now().toString();
        String sevenDaysAgo = LocalDate.now().minusDays(7).toString();
        return eventRepository.findAll().stream()
                .filter(e -> e.getDate() != null && e.getDate().compareTo(sevenDaysAgo) >= 0)
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
                .winners(event.getWinners())
                .clubId(event.getClub().getId()).clubName(event.getClub().getName())
                .attendeeCount(event.getAttendees().size()).isRegistered(isRegistered)
                .isFull(isFull).build();
    }

    public List<UserResponse> getEventAttendees(Long eventId, User currentUser) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));
        if (currentUser.getRole() == Role.COORDINATOR
                && (event.getClub().getCoordinator() == null || !event.getClub().getCoordinator().getId().equals(currentUser.getId())))
            throw new UnauthorizedException("Access denied.");
        
        return event.getAttendees().stream().map(u -> UserResponse.builder()
                .id(u.getId()).name(u.getName()).email(u.getEmail())
                .department(u.getDepartment()).year(u.getYear()).build()
        ).collect(Collectors.toList());
    }
}
