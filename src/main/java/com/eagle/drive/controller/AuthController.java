package com.eagle.drive.controller;

import com.eagle.drive.dto.LoginRequest;
import com.eagle.drive.dto.SignupRequest;
import com.eagle.drive.dto.AuthResponse;
import com.eagle.drive.model.User;
import com.eagle.drive.repository.UserRepository;
import com.eagle.drive.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "APIs for user signup, login, and token refresh")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // Temporary in-memory store. Replace with Redis or DB in production.
    private final Map<String, String> refreshTokens = new HashMap<>();

    // USER SIGNUP
    @Operation(summary = "Register a new user")
    @PostMapping("/signup")
    public ResponseEntity<?> register(@Valid @RequestBody SignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setLatitude(request.getLatitude());
        user.setLongitude(request.getLongitude());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() == null || request.getRole().isBlank() ? "RIDER" : request.getRole());
        user.setActive(true);

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        refreshTokens.put(refreshToken, user.getEmail());

        user.setPassword(null); // Hide hashed password

        AuthResponse authResponse = new AuthResponse(token, refreshToken, user);
        return ResponseEntity.ok(authResponse);
    }

    // USER LOGIN
    @Operation(summary = "Login user and return JWT & refresh token")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        User user = userOpt.get();
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        refreshTokens.put(refreshToken, user.getEmail());
        user.setPassword(null); // Hide hashed password

        return ResponseEntity.ok(new AuthResponse(token, refreshToken, user));
    }

    // REFRESH ACCESS TOKEN
    @Operation(summary = "Get new access token using refresh token")
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshAccessToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");

        if (refreshToken == null ||
                !jwtUtil.validateToken(refreshToken) ||
                !refreshTokens.containsKey(refreshToken)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid refresh token"));
        }

        String email = jwtUtil.extractUsername(refreshToken);
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        String newToken = jwtUtil.generateToken(email, userOpt.get().getRole());

        return ResponseEntity.ok(Map.of("token", newToken));
    }
}
