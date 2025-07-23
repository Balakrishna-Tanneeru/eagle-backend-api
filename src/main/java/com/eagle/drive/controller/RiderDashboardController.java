package com.eagle.drive.controller;

import com.eagle.drive.model.Ride;
import com.eagle.drive.model.User;
import com.eagle.drive.repository.RideRepository;
import com.eagle.drive.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/dashboard")
public class RiderDashboardController {

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/rides")
    @PreAuthorize("hasAnyRole('RIDER','DRIVER')")
    public ResponseEntity<List<Ride>> getRideHistory(Principal principal) {
        Optional<User> userOpt = userRepository.findByEmail(principal.getName());
        if (userOpt.isEmpty()) return ResponseEntity.status(401).build();

        User user = userOpt.get();
        List<Ride> rides = user.getRole().equals("RIDER")
                ? rideRepository.findByRider_IdOrderByCreatedAtDesc(user.getId())
                : rideRepository.findByDriver_IdOrderByCreatedAtDesc(user.getId());

        return ResponseEntity.ok(rides);
    }
}
