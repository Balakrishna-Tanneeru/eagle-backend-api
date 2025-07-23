package com.eagle.drive.controller;

import com.eagle.drive.model.Ride;
import com.eagle.drive.model.User;
import com.eagle.drive.repository.RideRepository;
import com.eagle.drive.repository.UserRepository;
import com.eagle.drive.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String riderToken;

    @BeforeEach
    void setup() {
        rideRepository.deleteAll();
        userRepository.deleteAll();

        // Create driver
        User driver = new User();
        driver.setName("Driver One");
        driver.setEmail("driver1@example.com");
        driver.setPassword("pass123");
        driver.setRole("DRIVER");
        driver.setActive(true);
        userRepository.save(driver);

        // Create rider
        User rider = new User();
        rider.setName("Rider One");
        rider.setEmail("rider1@example.com");
        rider.setPassword("pass123");
        rider.setRole("RIDER");
        rider.setActive(true);
        userRepository.save(rider);

        // Generate token
        riderToken = "Bearer " + jwtUtil.generateToken(rider.getEmail(), rider.getRole());
    }

    @Test
    void requestRide_withValidRiderToken_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/ride/request")
                        .param("pickup", "Point A")
                        .param("drop", "Point B")
                        .header("Authorization", riderToken))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Driver assigned")));

        List<Ride> rides = rideRepository.findAll();
        assertFalse(rides.isEmpty());
    }

    @Test
    void getMyRides_shouldReturnRidesForRider() throws Exception {
        // First request a ride
        mockMvc.perform(post("/api/ride/request")
                        .param("pickup", "Point A")
                        .param("drop", "Point B")
                        .header("Authorization", riderToken))
                .andExpect(status().isOk());

        // Fetch rides for rider
        mockMvc.perform(get("/api/ride/myrides")
                        .header("Authorization", riderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pickupLocation").value("Point A"));
    }

    @Test
    void requestRide_withoutToken_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/ride/request")
                        .param("pickup", "Point A")
                        .param("drop", "Point B"))
                .andExpect(status().isForbidden()); // Due to missing @PreAuthorize role
    }
}
