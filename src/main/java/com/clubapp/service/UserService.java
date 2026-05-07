package com.clubapp.service;

import com.clubapp.dto.request.*;
import com.clubapp.dto.response.UserResponse;
import com.clubapp.entity.Club;
import com.clubapp.entity.Role;
import com.clubapp.entity.User;
import com.clubapp.exception.BadRequestException;
import com.clubapp.exception.ConflictException;
import com.clubapp.exception.ResourceNotFoundException;
import com.clubapp.exception.UnauthorizedException;
import com.clubapp.repository.AttendanceRepository;
import com.clubapp.repository.ClubJoinRequestRepository;
import com.clubapp.repository.ClubRepository;
import com.clubapp.repository.EventRepository;
import com.clubapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final AttendanceRepository attendanceRepository;
    private final ClubJoinRequestRepository joinRequestRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.adminSecret}")
    private String adminSecret;

    // ── Self registration — STUDENT only ──────────────────────────────────
    @Transactional
    public UserResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail()))
            throw new ConflictException("Email already registered: " + req.getEmail());
        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.STUDENT)
                .department(req.getDepartment())
                .year(req.getYear())
                .build();
        return mapToResponse(userRepository.save(user));
    }

    // ── Admin registers with secret ────────────────────────────────────────
    @Transactional
    public UserResponse registerAdmin(RegisterRequest req, String secret) {
        if (!adminSecret.equals(secret))
            throw new UnauthorizedException("Invalid admin secret key.");
        if (userRepository.existsByEmail(req.getEmail()))
            throw new ConflictException("Email already registered.");
        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.ADMIN)
                .build();
        return mapToResponse(userRepository.save(user));
    }

    // ── Admin creates another admin account ───────────────────────────────
    @Transactional
    public UserResponse createAdmin(CreateCoordinatorRequest req, String secret) {
        if (!adminSecret.equals(secret))
            throw new UnauthorizedException("Invalid admin secret key.");
        if (userRepository.existsByEmail(req.getEmail()))
            throw new ConflictException("Email already registered: " + req.getEmail());
        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.ADMIN)
                .build();
        return mapToResponse(userRepository.save(user));
    }

    // ── Admin creates coordinator account ─────────────────────────────────
    @Transactional
    public UserResponse createCoordinator(CreateCoordinatorRequest req) {
        if (userRepository.existsByEmail(req.getEmail()))
            throw new ConflictException("Email already registered: " + req.getEmail());
        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.COORDINATOR)
                .department(req.getDepartment())
                .build();
        return mapToResponse(userRepository.save(user));
    }

    // ── Promote student to coordinator ────────────────────────────────────
    @Transactional
    public UserResponse promoteToCoordinator(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        if (user.getRole() != Role.STUDENT)
            throw new BadRequestException("Only students can be promoted.");
        user.setRole(Role.COORDINATOR);
        return mapToResponse(userRepository.save(user));
    }

    // ── Update own profile ─────────────────────────────────────────────────
    @Transactional
    public UserResponse updateProfile(User currentUser, UpdateProfileRequest req) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (req.getName()       != null && !req.getName().isBlank())  user.setName(req.getName());
        if (req.getDepartment() != null) user.setDepartment(req.getDepartment());
        if (req.getYear()       != null) user.setYear(req.getYear());
        if (req.getBio()        != null) user.setBio(req.getBio());
        return mapToResponse(userRepository.save(user));
    }

    // ── Change password ────────────────────────────────────────────────────
    @Transactional
    public void changePassword(User currentUser, ChangePasswordRequest req) {
        if (!req.getNewPassword().equals(req.getConfirmPassword()))
            throw new BadRequestException("New password and confirm password do not match.");
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword()))
            throw new BadRequestException("Current password is incorrect.");
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }

    // ── Queries ────────────────────────────────────────────────────────────
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<UserResponse> getAllCoordinators() {
        return userRepository.findByRole(Role.COORDINATOR).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        if (user.getRole() == Role.ADMIN)
            throw new UnauthorizedException("Cannot delete an admin account via this action. Use the super-admin delete with the secret key.");

        performUserCleanup(user);
        userRepository.delete(user);
    }

    // ── Super-admin delete: can remove any user including admins ──────────
    @Transactional
    public void deleteUserWithSecret(Long id, String secret, User currentUser) {
        if (!adminSecret.equals(secret))
            throw new UnauthorizedException("Invalid admin secret key.");
        User target = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        if (target.getId().equals(currentUser.getId()))
            throw new BadRequestException("You cannot delete your own account.");

        performUserCleanup(target);
        userRepository.delete(target);
    }

    private void performUserCleanup(User user) {
        Long userId = user.getId();
        
        // 1. Remove from club member lists and nullify coordinator if applicable
        List<com.clubapp.entity.Club> allClubs = clubRepository.findAll();
        for (com.clubapp.entity.Club club : allClubs) {
            club.getMembers().removeIf(m -> m.getId().equals(userId));
            if (club.getCoordinator() != null && club.getCoordinator().getId().equals(userId)) {
                club.setCoordinator(null);
            }
            clubRepository.save(club);
        }
        
        // 2. Delete attendance records
        attendanceRepository.deleteByUser(user);
        
        // 3. Delete club join requests
        joinRequestRepository.deleteByUser(user);
        
        // 4. Remove from all event attendee sets
        List<com.clubapp.entity.Event> events = eventRepository.findByAttendeesContaining(user);
        for (com.clubapp.entity.Event event : events) {
            event.getAttendees().removeIf(u -> u.getId().equals(userId));
            eventRepository.save(event);
        }
    }

    public UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .department(user.getDepartment())
                .year(user.getYear())
                .bio(user.getBio())
                .build();
    }
}
