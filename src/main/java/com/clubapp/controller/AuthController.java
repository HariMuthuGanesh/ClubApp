package com.clubapp.controller;

import com.clubapp.dto.request.RegisterRequest;
import com.clubapp.dto.request.LoginRequest;
import com.clubapp.dto.response.AuthResponse;
import com.clubapp.dto.response.UserResponse;
import com.clubapp.entity.User;
import com.clubapp.security.JwtUtil;
import com.clubapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    // Public student registration
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        UserResponse ur = userService.register(req);
        return ResponseEntity.ok(buildAuth(ur));
    }

    // Admin registration with secret key header
    @PostMapping("/register/admin")
    public ResponseEntity<AuthResponse> registerAdmin(
            @Valid @RequestBody RegisterRequest req,
            @RequestHeader("X-Admin-Secret") String secret) {
        UserResponse ur = userService.registerAdmin(req, secret);
        return ResponseEntity.ok(buildAuth(ur));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        User user = (User) auth.getPrincipal();
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(AuthResponse.builder()
                .token(token).name(user.getName()).email(user.getEmail()).role(user.getRole()).build());
    }

    private AuthResponse buildAuth(UserResponse ur) {
        String token = jwtUtil.generateToken(ur.getEmail(), ur.getRole().name());
        return AuthResponse.builder()
                .token(token).name(ur.getName()).email(ur.getEmail()).role(ur.getRole()).build();
    }
}
