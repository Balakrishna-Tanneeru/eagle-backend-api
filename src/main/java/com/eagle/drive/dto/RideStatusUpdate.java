package com.eagle.drive.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class RideStatusUpdate {
    private Long rideId;
    private String status;
    private String updatedBy;
    private LocalDateTime timestamp;

    private LocalDateTime estimatedArrivalTime;
    private String rideType;
    private Double driverRating;
}
