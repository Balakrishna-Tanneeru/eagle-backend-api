package com.eagle.drive.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdateRequest {
    private Long rideId;
    private double latitude;
    private double longitude;
}
