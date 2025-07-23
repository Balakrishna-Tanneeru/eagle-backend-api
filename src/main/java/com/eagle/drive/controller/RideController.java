package com.eagle.drive.controller;

import com.eagle.drive.model.Ride;
import com.eagle.drive.model.User;
import com.eagle.drive.repository.RideRepository;
import com.eagle.drive.repository.UserRepository;
import com.eagle.drive.dto.LocationUpdateRequest;
import com.eagle.drive.dto.RideRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/ride")
public class RideController {

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // ✅ Rider requests a new ride with driver matching
    @PostMapping("/request")
   // @PreAuthorize("hasRole('RIDER')")
    public ResponseEntity<Map<String, Object>> requestRide(
            @RequestBody RideRequest rideRequest,
            Principal principal
    ) {
        Optional<User> riderOpt = userRepository.findByEmail(principal.getName());
        if (riderOpt.isEmpty())
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

        User rider = riderOpt.get();
        User driver = findNearestAvailableDriver(rideRequest.getPickupLat(), rideRequest.getPickupLng());
        if (driver == null)
            return ResponseEntity.status(404).body(Map.of("error", "No available drivers nearby"));

        Ride ride = new Ride();
        ride.setRider(rider);
        ride.setDriver(driver);
        ride.setPickupLocation(rideRequest.getPickupAddress());
        ride.setDropLocation(rideRequest.getDropAddress());
        ride.setFare(rideRequest.getFare());
        ride.setStatus(rideRequest.getStatus());

        rideRepository.save(ride);

        return ResponseEntity.ok(Map.of(
                "rideId", ride.getId(),
                "message", "Ride booked",
                "driverName", driver.getName()
        ));
    }


    // ✅ Get ride history for rider or driver
    @GetMapping("/myrides")
    public ResponseEntity<List<Ride>> getMyRides(Principal principal) {
        Optional<User> userOpt = userRepository.findByEmail(principal.getName());
        if (userOpt.isEmpty()) return ResponseEntity.status(401).build();

        User user = userOpt.get();
        List<Ride> rides = user.getRole().equals("RIDER")
                ? rideRepository.findByRider_Id(user.getId())
                : rideRepository.findByDriver_Id(user.getId());
System.out.println("rides:..."+rides);
        return ResponseEntity.ok(rides);
    }

    // ✅ Location broadcast via WebSocket
    @MessageMapping("/location")
    public void broadcastLocation(LocationUpdateRequest request) {
        messagingTemplate.convertAndSend("/topic/driver/" + request.getRideId(), request);
    }

    // ✅ REST endpoint for drivers to update location
 //   @PreAuthorize("hasRole('DRIVER')")
    @PostMapping("/location/update")
    public ResponseEntity<String> updateDriverLocation(
            @RequestBody LocationUpdateRequest request,
            Principal principal
    ) {
        Optional<User> driverOpt = userRepository.findByEmail(principal.getName());
        if (driverOpt.isEmpty() || !"DRIVER".equals(driverOpt.get().getRole())) {
            return ResponseEntity.status(403).body("Only drivers can update location");
        }

        User driver = driverOpt.get();
        driver.setLatitude(request.getLatitude());
        driver.setLongitude(request.getLongitude());
        userRepository.save(driver);

        messagingTemplate.convertAndSend("/topic/driver/" + request.getRideId(), request);
        return ResponseEntity.ok("Location updated");
    }

    // -----------------------
    // 🔍 Utility Methods
    // -----------------------

    private User findNearestAvailableDriver(double riderLat, double riderLng) {
        List<User> drivers = userRepository.findByRoleAndActive("DRIVER", true);
        User bestDriver = null;
        double minDistance = Double.MAX_VALUE;

        for (User driver : drivers) {
            if (driver.getLatitude() == null || driver.getLongitude() == null) continue;

            boolean isBusy = rideRepository.existsByDriver_IdAndStatusIn(driver.getId(), List.of("REQUESTED", "STARTED"));
            if (isBusy) continue;

            double distance = haversine(riderLat, riderLng, driver.getLatitude(), driver.getLongitude());
            if (distance < minDistance) {
                minDistance = distance;
                bestDriver = driver;
            }
        }

        return bestDriver;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in KM
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private double estimateFare(double lat1, double lon1, double lat2, double lon2) {
        double distance = haversine(lat1, lon1, lat2, lon2);
        return Math.round((distance * 15) + 30); // base fare + per km
    }
}
