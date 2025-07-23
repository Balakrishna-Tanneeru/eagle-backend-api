package com.eagle.drive.dto;

import lombok.Data;

@Data
public class RideRequest {
    private double pickupLat;
    private double pickupLng;
    private String pickupAddress;
    private String dropAddress;
    private String status;
    private String timestamp;
    private double distance;
    private double fare;
}

