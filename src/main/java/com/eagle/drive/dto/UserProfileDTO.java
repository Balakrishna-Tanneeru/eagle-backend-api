package com.eagle.drive.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Default constructor
@AllArgsConstructor // Constructor with all fields
public class UserProfileDTO {
    private String name;
    private String email;
    private String role;
    private boolean active;
    private String phone;
}
