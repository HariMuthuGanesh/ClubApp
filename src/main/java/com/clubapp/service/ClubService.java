package com.clubapp.service;

import com.clubapp.dto.request.CreateClubRequest;
import com.clubapp.dto.response.ClubResponse;
import com.clubapp.entity.*;
import com.clubapp.exception.ResourceNotFoundException;
import com.clubapp.repository.ClubJoinRequestRepository;
import com.clubapp.repository.ClubRepository;
import com.clubapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;
    private final UserRepository userRepository;
    private final ClubJoinRequestRepository joinRequestRepository;

    @Transactional
    public ClubResponse createClub(CreateClubRequest req) {
        if (clubRepository.existsByNameIgnoreCase(req.getName()))
            throw new IllegalArgumentException("A club with this name already exists.");
        User coordinator = userRepository.findByEmail(req.getCoordinatorEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Coordinator not found: " + req.getCoordinatorEmail()));
        if (coordinator.getRole() != Role.COORDINATOR)
            throw new IllegalArgumentException("The specified user is not a Coordinator.");
        Club club = Club.builder()
                .name(req.getName()).description(req.getDescription())
                .vision(req.getVision()).mission(req.getMission())
                .department(req.getDepartment()).foundedYear(req.getFoundedYear())
                .coordinator(coordinator).build();
        return mapToResponse(clubRepository.save(club), null);
    }

    public List<ClubResponse> getAllClubs(User currentUser) {
        return clubRepository.findAll().stream()
                .map(c -> mapToResponse(c, currentUser)).collect(Collectors.toList());
    }

    public ClubResponse getClubById(Long id, User currentUser) {
        return mapToResponse(clubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found: " + id)), currentUser);
    }

    public List<ClubResponse> getMyClubs(User currentUser) {
        return clubRepository.findAll().stream()
                .filter(c -> c.getMembers().stream().anyMatch(m -> m.getId().equals(currentUser.getId())))
                .map(c -> mapToResponse(c, currentUser)).collect(Collectors.toList());
    }

    public Optional<ClubResponse> getClubByCoordinator(User coordinator) {
        return clubRepository.findByCoordinator(coordinator)
                .map(c -> mapToResponse(c, coordinator));
    }

    @Transactional
    public ClubResponse updateClubInfo(Long clubId, CreateClubRequest req, User currentUser) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found: " + clubId));
        if (currentUser.getRole() == Role.COORDINATOR
                && !club.getCoordinator().getId().equals(currentUser.getId()))
            throw new IllegalArgumentException("You are not the coordinator of this club.");
        if (req.getDescription() != null) club.setDescription(req.getDescription());
        if (req.getVision()      != null) club.setVision(req.getVision());
        if (req.getMission()     != null) club.setMission(req.getMission());
        return mapToResponse(clubRepository.save(club), currentUser);
    }

    @Transactional
    public void deleteClub(Long id) {
        clubRepository.delete(clubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found: " + id)));
    }

    @Transactional
    public void updateLogoImage(Long clubId, String filename) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found: " + clubId));
        club.setLogoImage(filename);
        clubRepository.save(club);
    }

    public ClubResponse mapToResponse(Club club, User currentUser) {
        boolean isMember = currentUser != null && club.getMembers().stream()
                .anyMatch(m -> m.getId().equals(currentUser.getId()));
        String joinStatus = "NONE";
        if (currentUser != null && !isMember) {
            Optional<ClubJoinRequest> req = joinRequestRepository.findByUserAndClub(currentUser, club);
            if (req.isPresent()) joinStatus = req.get().getStatus().name();
        } else if (isMember) {
            joinStatus = "ACCEPTED";
        }
        return ClubResponse.builder()
                .id(club.getId()).name(club.getName()).description(club.getDescription())
                .vision(club.getVision()).mission(club.getMission()).department(club.getDepartment())
                .foundedYear(club.getFoundedYear()).logoImage(club.getLogoImage())
                .coordinatorName(club.getCoordinator().getName())
                .coordinatorEmail(club.getCoordinator().getEmail())
                .coordinatorId(club.getCoordinator().getId())
                .memberCount(club.getMembers().size()).eventCount(club.getEvents().size())
                .isMember(isMember).joinRequestStatus(joinStatus).build();
    }
}
