package com.clubapp.service;

import com.clubapp.dto.response.JoinRequestResponse;
import com.clubapp.dto.response.UserResponse;
import com.clubapp.entity.*;
import com.clubapp.exception.BadRequestException;
import com.clubapp.exception.ConflictException;
import com.clubapp.exception.ResourceNotFoundException;
import com.clubapp.exception.UnauthorizedException;
import com.clubapp.repository.ClubJoinRequestRepository;
import com.clubapp.repository.ClubRepository;
import com.clubapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClubJoinRequestService {

    private final ClubJoinRequestRepository joinRequestRepository;
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Transactional
    public JoinRequestResponse sendRequest(Long clubId, User user, String message) {
        if (user.getRole() == Role.ADMIN)
            throw new BadRequestException("Admin cannot join clubs.");
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found: " + clubId));
        if (club.getMembers().stream().anyMatch(m -> m.getId().equals(user.getId())))
            throw new ConflictException("You are already a member of this club.");
        Optional<ClubJoinRequest> existing = joinRequestRepository.findByUserAndClub(user, club);
        if (existing.isPresent()) {
            ClubJoinRequest req = existing.get();
            if (req.getStatus() == JoinRequestStatus.PENDING)
                throw new ConflictException("You already have a pending request.");
            req.setStatus(JoinRequestStatus.PENDING);
            req.setMessage(message);
            req.setRequestedAt(LocalDateTime.now());
            req.setRespondedAt(null);
            return mapToResponse(joinRequestRepository.save(req));
        }
        return mapToResponse(joinRequestRepository.save(ClubJoinRequest.builder()
                .user(user).club(club).message(message).status(JoinRequestStatus.PENDING).build()));
    }

    @Transactional
    public JoinRequestResponse acceptRequest(Long requestId, User coordinator) {
        ClubJoinRequest request = getAndValidate(requestId, coordinator);
        request.setStatus(JoinRequestStatus.ACCEPTED);
        request.setRespondedAt(LocalDateTime.now());
        Club club = request.getClub();
        club.getMembers().add(request.getUser());
        clubRepository.save(club);
        return mapToResponse(joinRequestRepository.save(request));
    }

    @Transactional
    public JoinRequestResponse rejectRequest(Long requestId, User coordinator) {
        ClubJoinRequest request = getAndValidate(requestId, coordinator);
        request.setStatus(JoinRequestStatus.REJECTED);
        request.setRespondedAt(LocalDateTime.now());
        return mapToResponse(joinRequestRepository.save(request));
    }

    public List<JoinRequestResponse> getPendingRequests(User coordinator) {
        Club club = clubRepository.findByCoordinator(coordinator)
                .orElseThrow(() -> new ResourceNotFoundException("No club assigned to you."));
        return joinRequestRepository.findByClubAndStatus(club, JoinRequestStatus.PENDING)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<JoinRequestResponse> getAllRequestsForClub(User coordinator) {
        Club club = clubRepository.findByCoordinator(coordinator)
                .orElseThrow(() -> new ResourceNotFoundException("No club assigned to you."));
        return joinRequestRepository.findByClub(club)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<JoinRequestResponse> getMyRequests(User user) {
        return joinRequestRepository.findByUser(user)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<UserResponse> getClubMembers(User coordinator) {
        Club club = clubRepository.findByCoordinator(coordinator)
                .orElseThrow(() -> new ResourceNotFoundException("No club assigned to you."));
        return club.getMembers().stream().map(userService::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public UserResponse promoteToCoordinator(Long userId, User coordinator) {
        Club club = clubRepository.findByCoordinator(coordinator)
                .orElseThrow(() -> new ResourceNotFoundException("No club assigned to you."));
        User student = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        if (club.getMembers().stream().noneMatch(m -> m.getId().equals(userId)))
            throw new BadRequestException("User is not a member of your club.");
        student.setRole(Role.COORDINATOR);
        club.setCoordinator(student);
        clubRepository.save(club);
        return userService.mapToResponse(userRepository.save(student));
    }

    private ClubJoinRequest getAndValidate(Long requestId, User coordinator) {
        ClubJoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + requestId));
        if (coordinator.getRole() != Role.ADMIN
                && (request.getClub().getCoordinator() == null || !request.getClub().getCoordinator().getId().equals(coordinator.getId())))
            throw new UnauthorizedException("You are not the coordinator of this club.");
        return request;
    }

    public JoinRequestResponse mapToResponse(ClubJoinRequest req) {
        return JoinRequestResponse.builder()
                .id(req.getId()).userId(req.getUser().getId()).userName(req.getUser().getName())
                .userEmail(req.getUser().getEmail()).userDepartment(req.getUser().getDepartment())
                .userYear(req.getUser().getYear()).clubId(req.getClub().getId())
                .clubName(req.getClub().getName()).status(req.getStatus())
                .message(req.getMessage()).requestedAt(req.getRequestedAt())
                .respondedAt(req.getRespondedAt()).build();
    }
}
