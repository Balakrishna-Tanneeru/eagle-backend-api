package com.eagle.drive.controller;

import com.eagle.drive.dto.UserProfileDTO;
import com.eagle.drive.model.Ride;
import com.eagle.drive.model.User;
import com.eagle.drive.repository.RideRepository;
import com.eagle.drive.repository.UserRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "bearer-jwt") // Enables JWT auth in Swagger
@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RideRepository rideRepository;

    // ✅ Get user profile for any authenticated role
    @GetMapping("/profile")
 //   @PreAuthorize("permitAll()") // just to verify controller works

    public ResponseEntity<UserProfileDTO> getProfile(Authentication auth) {
        log.info("1 Entered profile controller");

        String email = auth.getName();
      //  String email = "bk@gmail.com";

        log.info("2 email after............");
        log.info("🔐 Authenticated request for profile: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("❌ User not found for email: " + email));
        log.info("3 after findByEmail............");
        UserProfileDTO profile = new UserProfileDTO(
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getPhone()
        );
        log.info("4 User profile data: {}", profile);

        log.info("✅ Returning profile for {} with role: {}", email, user.getRole());
        return ResponseEntity.ok(profile);
    }

    // ✅ Get ride history for rider
    @GetMapping("/rides")
   // @PreAuthorize("hasAuthority('RIDER')")
    public ResponseEntity<List<Ride>> getRides(Authentication auth) {
        String email = auth.getName();
        log.info("📥 Fetching rides for: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("❌ Rider not found for email: " + email));

        List<Ride> rides = rideRepository.findByRider_Id(user.getId());

        log.info("✅ Found {} rides for: {}", rides.size(), email);
        return ResponseEntity.ok(rides);
    }
}
