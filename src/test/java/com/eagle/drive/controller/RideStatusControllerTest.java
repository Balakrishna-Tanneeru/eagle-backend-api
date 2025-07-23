package com.eagle.drive.controller;

import com.eagle.drive.model.Ride;
import com.eagle.drive.model.User;
import com.eagle.drive.repository.RideRepository;
import com.eagle.drive.repository.UserRepository;
import com.eagle.drive.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RideStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private Ride ride;
    private String driverToken;

    @BeforeEach
    void setup() {
        rideRepository.deleteAll();
        userRepository.deleteAll();

        // Create driver
        User driver = new User();
        driver.setName("Driver1");
        driver.setEmail("driver1@example.com");
        driver.setPassword("pass123");
        driver.setRole("DRIVER");
        driver.setActive(true);
        userRepository.save(driver);

        // Create rider
        User rider = new User();
        rider.setName("Rider1");
        rider.setEmail("rider1@example.com");
        rider.setPassword("pass123");
        rider.setRole("RIDER");
        rider.setActive(true);
        userRepository.save(rider);

        // Create ride
        ride = new Ride();
        ride.setPickupLocation("A");
        ride.setDropLocation("B");
        ride.setStatus("REQUESTED");
        ride.setDriver(driver);
        ride.setRider(rider);
        ride.setFare(120.0);
        rideRepository.save(ride);

        driverToken = "Bearer " + jwtUtil.generateToken(driver.getEmail(), driver.getRole());
    }

    @Test
    void acceptRide_shouldUpdateStatus() throws Exception {
        mockMvc.perform(post("/api/ride/status/" + ride.getId() + "/accept")
                        .header("Authorization", driverToken))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ACCEPTED")));

        Ride updated = rideRepository.findById(ride.getId()).get();
        assertEquals("ACCEPTED", updated.getStatus());
    }

    @Test
    void startRide_shouldUpdateStatus() throws Exception {
        ride.setStatus("ACCEPTED");
        rideRepository.save(ride);

        mockMvc.perform(post("/api/ride/status/" + ride.getId() + "/start")
                        .header("Authorization", driverToken))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("STARTED")));

        Ride updated = rideRepository.findById(ride.getId()).get();
        assertEquals("STARTED", updated.getStatus());
    }

    @Test
    void completeRide_shouldUpdateStatus() throws Exception {
        ride.setStatus("STARTED");
        rideRepository.save(ride);

        mockMvc.perform(post("/api/ride/status/" + ride.getId() + "/complete")
                        .header("Authorization", driverToken))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("COMPLETED")));

        Ride updated = rideRepository.findById(ride.getId()).get();
        assertEquals("COMPLETED", updated.getStatus());
    }

    @Test
    void cancelRide_shouldUpdateStatus() throws Exception {
        mockMvc.perform(post("/api/ride/status/" + ride.getId() + "/cancel")
                        .header("Authorization", driverToken))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("CANCELLED")));

        Ride updated = rideRepository.findById(ride.getId()).get();
        assertEquals("CANCELLED", updated.getStatus());
    }

    @Test
    void rejectRide_shouldUpdateStatus() throws Exception {
        mockMvc.perform(post("/api/ride/status/" + ride.getId() + "/reject")
                        .header("Authorization", driverToken))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("REJECTED")));

        Ride updated = rideRepository.findById(ride.getId()).get();
        assertEquals("REJECTED", updated.getStatus());
    }
}
