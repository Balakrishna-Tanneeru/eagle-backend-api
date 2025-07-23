package com.eagle.drive.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User rider;

    @ManyToOne
    private User driver;

    private String pickupLocation;
    private String dropLocation;

    private Double fare;

    private String status; // REQUESTED, ACCEPTED, STARTED, COMPLETED, CANCELLED

    private LocalDateTime cancelledAt;
    private String cancelledBy;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    private String rideType; // e.g., "STANDARD", "PREMIUM"

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
