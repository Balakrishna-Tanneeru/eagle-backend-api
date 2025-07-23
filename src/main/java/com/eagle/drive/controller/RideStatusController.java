package com.eagle.drive.controller;

import com.eagle.drive.dto.RideStatusUpdate;
import com.eagle.drive.model.Ride;
import com.eagle.drive.repository.RideRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/ride/status")
public class RideStatusController {

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PreAuthorize("hasRole('DRIVER')")
    @PostMapping("/{rideId}/accept")
    public ResponseEntity<String> acceptRide(@PathVariable Long rideId) {
        return updateRideStatus(rideId, "ACCEPTED", null);
    }

    @PreAuthorize("hasRole('DRIVER')")
    @PostMapping("/{rideId}/reject")
    public ResponseEntity<String> rejectRide(@PathVariable Long rideId) {
        return updateRideStatus(rideId, "REJECTED", null);
    }

    @PreAuthorize("hasRole('DRIVER')")
    @PostMapping("/{rideId}/start")
    public ResponseEntity<String> startRide(@PathVariable Long rideId) {
        return updateRideStatus(rideId, "STARTED", null);
    }

    @PreAuthorize("hasRole('DRIVER')")
    @PostMapping("/{rideId}/complete")
    public ResponseEntity<String> completeRide(@PathVariable Long rideId) {
        return updateRideStatus(rideId, "COMPLETED", null);
    }

    @PreAuthorize("hasRole('DRIVER')")
    @PostMapping("/{rideId}/cancel")
    public ResponseEntity<String> cancelRide(@PathVariable Long rideId) {
        return updateRideStatus(rideId, "CANCELLED", "DRIVER");
    }

    @PreAuthorize("hasRole('RIDER')")
    @PostMapping("/{rideId}/cancel-by-rider")
    public ResponseEntity<String> cancelRideByRider(@PathVariable Long rideId, Principal principal) {
        Optional<Ride> rideOpt = rideRepository.findById(rideId);
        if (rideOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Ride ride = rideOpt.get();
        if (!ride.getRider().getEmail().equals(principal.getName())) {
            return ResponseEntity.status(403).body("You are not authorized to cancel this ride.");
        }

        if ("STARTED".equalsIgnoreCase(ride.getStatus()) || "COMPLETED".equalsIgnoreCase(ride.getStatus())) {
            return ResponseEntity.badRequest().body("Cannot cancel a ride that has already started or completed.");
        }

        return updateRideStatus(rideId, "CANCELLED", "RIDER");
    }

    private ResponseEntity<String> updateRideStatus(Long rideId, String status, String cancelledBy) {
        Optional<Ride> rideOpt = rideRepository.findById(rideId);
        if (rideOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Ride ride = rideOpt.get();
        ride.setStatus(status);
        LocalDateTime now = LocalDateTime.now();

        switch (status.toUpperCase()) {
            case "CANCELLED" -> {
                ride.setCancelledAt(now);
                ride.setCancelledBy(cancelledBy);
            }
            case "STARTED" -> ride.setStartedAt(now);
            case "COMPLETED" -> ride.setCompletedAt(now);
        }

        // Mock data for now; calculate or store this dynamically in real apps
        LocalDateTime estimatedArrivalTime = now.plusMinutes(10); // placeholder
        String rideType = ride.getRideType() != null ? ride.getRideType() : "STANDARD";
        Double driverRating = 4.6; // assume static or fetch from ratings table

        RideStatusUpdate update = new RideStatusUpdate(
                ride.getId(),
                ride.getStatus(),
                cancelledBy != null ? cancelledBy : ride.getDriver().getEmail(),
                now,
                estimatedArrivalTime,
                rideType,
                driverRating
        );

        messagingTemplate.convertAndSend("/topic/ride-status/" + rideId, update);
        rideRepository.save(ride);

        return ResponseEntity.ok("Ride status updated to " + status);
    }

}
