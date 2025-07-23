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
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RideAuthorizationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private RideRepository rideRepository;
    @Autowired private JwtUtil jwtUtil;

    private Long rideId;
    private String riderToken;
    private String driverToken;

    @BeforeEach
    void setUp() {
        rideRepository.deleteAll();
        userRepository.deleteAll();

        // Create driver
        User driver = new User();
        driver.setName("Driver");
        driver.setEmail("driver@example.com");
        driver.setPassword("pass");
        driver.setRole("DRIVER");
        driver.setActive(true);
        userRepository.save(driver);

        // Create rider
        User rider = new User();
        rider.setName("Rider");
        rider.setEmail("rider@example.com");
        rider.setPassword("pass");
        rider.setRole("RIDER");
        rider.setActive(true);
        userRepository.save(rider);

        // Create ride
        Ride ride = new Ride();
        ride.setDriver(driver);
        ride.setRider(rider);
        ride.setPickupLocation("X");
        ride.setDropLocation("Y");
        ride.setFare(99.0);
        ride.setStatus("REQUESTED");
        rideRepository.save(ride);
        rideId = ride.getId();

        // Generate JWTs
        driverToken = "Bearer " + jwtUtil.generateToken(driver.getEmail(), driver.getRole());
        riderToken  = "Bearer " + jwtUtil.generateToken(rider.getEmail(), rider.getRole());
    }

    // ✅ DRIVER can accept ride
    @Test
    void driverCanAcceptRide() throws Exception {
        mockMvc.perform(post("/api/ride/status/" + rideId + "/accept")
                        .header("Authorization", driverToken))
                .andExpect(status().isOk());
    }

    // ❌ RIDER cannot accept ride
    @Test
    void riderCannotAcceptRide() throws Exception {
        mockMvc.perform(post("/api/ride/status/" + rideId + "/accept")
                        .header("Authorization", riderToken))
                .andExpect(status().isForbidden());
    }

    // ✅ RIDER can request ride
    @Test
    void riderCanRequestRide() throws Exception {
        mockMvc.perform(post("/api/ride/request")
                        .param("pickup", "Point A")
                        .param("drop", "Point B")
                        .header("Authorization", riderToken))
                .andExpect(status().isOk());
    }

    // ❌ DRIVER cannot request ride
    @Test
    void driverCannotRequestRide() throws Exception {
        mockMvc.perform(post("/api/ride/request")
                        .param("pickup", "Point A")
                        .param("drop", "Point B")
                        .header("Authorization", driverToken))
                .andExpect(status().isForbidden());
    }
}
